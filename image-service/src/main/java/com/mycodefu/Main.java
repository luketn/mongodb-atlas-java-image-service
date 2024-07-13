package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mycodefu.atlas.AtlasSearchBuilder;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    public static final String CONNECTION_STRING = System.getenv("CONNECTION_STRING") != null ? System.getenv("CONNECTION_STRING") : "mongodb://localhost:27017";

    public record Photo(String caption, String filename) {}

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
        System.out.println("Received request:\n" + apiGatewayV2HTTPEvent);

        //return cacheable options
        String method = apiGatewayV2HTTPEvent.getRequestContext().getHttp().getMethod();
        String path = apiGatewayV2HTTPEvent.getRequestContext().getHttp().getPath();
        switch(method) {
            case "GET": {
                switch (path) {
                    case "/photos": {
                        //get the caption query string parameter
                        String caption = apiGatewayV2HTTPEvent.getQueryStringParameters().getOrDefault("caption", "No caption");

                        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
                        MongoDatabase imageSearch = mongoClient.getDatabase("ImageSearch");
                        MongoCollection<Photo> photosCollection = imageSearch.getCollection("photos", Photo.class);

                        AggregateIterable<Photo> photosIterator = photosCollection.aggregate(AtlasSearchBuilder.builder()
                                .withCaption(caption)
                                .build(
                                        Aggregates.limit(5)
                                )
                        );

                        ArrayList<Photo> photos = photosIterator.into(new ArrayList<>());

                        //serialise to JSON
                        List<BsonValue> bsonPhotoList = photos.stream().map(photo -> {
                            BsonDocument bsonPhoto = new BsonDocument();
                            bsonPhoto.append("caption", new BsonString(photo.caption()));
                            String url = "https://image-search.mycodefu.com/" + photo.filename();
                            bsonPhoto.append("url", new BsonString(url));
                            return bsonPhoto;
                        }).collect(Collectors.toList());
                        BsonDocument bsonDocument = new BsonDocument(
                                "photos", new BsonArray(bsonPhotoList)
                        );
                        String json = bsonDocument.toJson();

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
}