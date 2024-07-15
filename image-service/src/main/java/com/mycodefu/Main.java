package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mycodefu.atlas.AtlasSearchBuilder;
import org.bson.*;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    public static final String CONNECTION_STRING = System.getenv("CONNECTION_STRING") != null ? System.getenv("CONNECTION_STRING") : "mongodb://localhost:27017/directConnection=true";
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static MongoClient connect() {
        long start = System.currentTimeMillis();
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        long end = System.currentTimeMillis();

        log.info("Connected to MongoDB at {} in {}ms", CONNECTION_STRING.replaceAll("://.*@", "://<redacted>@"), end - start);
        return mongoClient;
    }
    public static final MongoClient MONGO_CLIENT = connect();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
        if (log.isTraceEnabled()) {
            log.trace("Received request:\n{}", apiGatewayV2HTTPEvent);
        }

        //return cacheable options
        String method = apiGatewayV2HTTPEvent.getRequestContext().getHttp().getMethod();
        String path = apiGatewayV2HTTPEvent.getRequestContext().getHttp().getPath();
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        switch(method) {
            case "GET": {
                switch (path) {
                    case "/colours": {
                        
                        //distinct colours
                        String[] distinctColours = new String[]{
                                "Beige", "Black", "Blue",
                                "Brown",
                                "Cream",
                                "Dark Brown", "Dark Grey",// convert to Brown: "Darker Brown",
                                "Golden",// convert to Golden: "Gold",  "Golden Brown",
                                "Green", "Grey",
                                "Light Brown", "Light Grey",
                                "Light Tan", "Orange", "Pink",
                                "Purple", "Red", "Silver",
                                "Tan", "White",
                                "Yellow"
                        };
                        String json = Arrays.stream(distinctColours)
                                .sorted()
                                .map(colour -> "\"" + colour + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));
                        
                        return APIGatewayV2HTTPResponse.builder()
                                .withStatusCode(200)
                                .withBody(json)
                                .build();
                    }
                    case "/photos": {
                        String json;
                        try {
                            //get the caption query string parameter
                            String caption = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault("caption", null);
                            String hasPersonString = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault("hasPerson", null);
                            Boolean hasPerson = hasPersonString != null ? Boolean.parseBoolean(hasPersonString) : null;
                            List<String> colours = getStringListQueryParams(apiGatewayV2HTTPEvent, "colours");
                            List<String> breeds = getStringListQueryParams(apiGatewayV2HTTPEvent, "breeds");
                            List<String> sizes = getStringListQueryParams(apiGatewayV2HTTPEvent, "sizes");

                            //Get the MongoDB collection
                            MongoDatabase imageSearch = MONGO_CLIENT.getDatabase("ImageSearch");
                            MongoCollection<BsonDocument> photosCollection = imageSearch.getCollection("photo", BsonDocument.class);

                            //Perform Atlas Search query
                            if ((caption == null || caption.isEmpty()) && hasPerson == null && colours == null && breeds == null && sizes == null) {
                                return APIGatewayV2HTTPResponse.builder()
                                        .withStatusCode(400)
                                        .withBody("Must provide at least one query parameter")
                                        .build();
                            }
                            List<Bson> query = AtlasSearchBuilder.builder()
                                    .withCaption(caption)
                                    .hasPerson(hasPerson)
                                    .withColours(colours)
                                    .withBreeds(breeds)
                                    .withSizes(sizes)
                                    .build(
                                            Aggregates.limit(5),
                                            Aggregates.addFields(
                                                    new Field<>("id", new Document("$toString", "$_id"))
                                            ),
                                            Aggregates.project(
                                                    new Document()
                                                            .append("_id", 0)
                                                            .append("runData", 0)
                                            )
                                    );

                            //Log the query
                            if (log.isTraceEnabled()) {
                                var aggregateClauses = query
                                        .stream()
                                        .map(bson -> bson.toBsonDocument().toJson())
                                        .collect(Collectors.joining(",\n    "))
                                        ;
                                log.trace("""
                                        Atlas Search query:
                                          db.photo.aggregate([
                                            %s
                                          ])
                                        """.formatted(aggregateClauses)
                                );
                            }

                            AggregateIterable<BsonDocument> photosIterator = photosCollection.aggregate(query);
                            ArrayList<BsonDocument> photos = photosIterator.into(new ArrayList<>());

                            //serialise to JSON
                            BsonDocument bsonDocument = new BsonDocument("photos", new BsonArray(photos));
                            json = bsonDocument.toJson();
                        } catch (Exception e) {
                            log.error("Error in /photos api", e);
                            return APIGatewayV2HTTPResponse.builder()
                                    .withStatusCode(500)
                                    .withBody("Error: " + e.getMessage())
                                    .build();
                        }

                        return APIGatewayV2HTTPResponse.builder()
                                .withStatusCode(200)
                                .withBody(json)
                                .build();
                    }
                    case "/":
                    case "/index.html": {
                        String indexFile;
                        try {
                            InputStream inputStream = getClass().getResourceAsStream("/index.html");
                            if (inputStream == null) {
                                throw new RuntimeException("index.html not found");
                            }
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                                indexFile = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                            }
                        } catch (IOException e) {
                            log.error("Error reading index.html", e);
                            throw new RuntimeException(e);
                        }
                        return APIGatewayV2HTTPResponse.builder()
                                .withStatusCode(200)
                                .withBody(indexFile)
                                .withHeaders(Map.of("Content-Type", "text/html"))
                                .build();
                    }
                    default: {
                        return APIGatewayV2HTTPResponse.builder()
                                .withStatusCode(404)
                                .withBody("Not found")
                                .build();
                    }
                }
            }
            case "OPTIONS": {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(200)
                        .withHeaders(Map.of(
                                "Access-Control-Allow-Origin", "*",
                                "Access-Control-Allow-Methods", "POST, GET, OPTIONS",
                                "Access-Control-Allow-Headers", "*",
                                "Access-Control-Max-Age", "86400"
                        ))
                        .build();
            }
            default: {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(405)
                        .withBody("Method not allowed")
                        .build();
                }
        }
    }

    private static List<String> getStringListQueryParams(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, String paramName) {
        String paramString = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault(paramName, null);
        List<String> paramValues = paramString != null ? List.of(paramString.split(",")) : null;
        return paramValues;
    }
}