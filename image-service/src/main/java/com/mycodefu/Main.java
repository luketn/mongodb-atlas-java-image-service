package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mycodefu.atlas.AtlasSearchBuilder;
import com.mycodefu.atlas.MongoConnection;
import com.mycodefu.data.Colours;
import com.mycodefu.data.Photo;
import com.mycodefu.data.PhotoResults;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mycodefu.atlas.MongoConnection.connection;
import static com.mycodefu.data.Serializer.objectMapper;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

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
                        String json;
                        try {
                            json = objectMapper.writeValueAsString(Colours.allowed);
                        } catch (JsonProcessingException e) {
                            log.error("Error serialising colours", e);
                            throw new RuntimeException(e);
                        }
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
                            String summary = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault("summary", null);
                            String hasPersonString = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault("hasPerson", null);
                            Boolean hasPerson = hasPersonString != null ? Boolean.parseBoolean(hasPersonString) : null;
                            List<String> colours = getStringListQueryParams(apiGatewayV2HTTPEvent, "colours");
                            List<String> breeds = getStringListQueryParams(apiGatewayV2HTTPEvent, "breeds");
                            List<String> sizes = getStringListQueryParams(apiGatewayV2HTTPEvent, "sizes");

                            //Get the MongoDB collection
                            MongoDatabase imageSearch = connection().getDatabase("ImageSearch");
                            MongoCollection<Photo> photosCollection = imageSearch.getCollection("photo", Photo.class);

                            //Perform Atlas Search query
                            if (
                                 (caption == null || caption.isEmpty())
                                 && (summary == null || summary.isEmpty())
                                 && hasPerson == null
                                 && colours == null
                                 && breeds == null
                                 && sizes == null
                            ) {
                                return APIGatewayV2HTTPResponse.builder()
                                        .withStatusCode(400)
                                        .withBody("Must provide at least one query parameter")
                                        .build();
                            }
                            List<Bson> query = AtlasSearchBuilder.builder()
                                    .withCaption(caption)
                                    .withSummary(summary)
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

                                AggregateIterable<Photo> photosIterator = photosCollection.aggregate(query);
                            ArrayList<Photo> photos = photosIterator.into(new ArrayList<>());

                            //serialise to JSON
                            PhotoResults photoResults = new PhotoResults(photos);
                            json = objectMapper.writeValueAsString(photoResults);

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