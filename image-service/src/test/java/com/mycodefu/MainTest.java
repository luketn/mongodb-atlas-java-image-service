package com.mycodefu;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mycodefu.atlas.MongoConnection;
import com.mycodefu.data.Dog;
import com.mycodefu.data.DogSize;
import com.mycodefu.data.Photo;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.AtlasMongoDBTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainTest extends AtlasMongoDBTest {

    @BeforeAll
    static void setup() throws IOException {
        MongoConnection.setConnectionString(connectionString());
        //read index from file path "create-index.json"
        String indexResource = Files.readString(Paths.get("create-index.json"));
        MongoConnection.createAtlasIndex(
                "ImageSearch",
                "photo",
                Photo.class,
                "default",
                BsonDocument.parse(indexResource),
                List.of(
                    new Photo("A pair of whippets in red bandannas.",
                            "https://image-search.mycodefu.com/photos/Images/n02091134-whippet/n02091134_19308.jpg",
                            false,
                            List.of(new Dog(List.of("White"), "Whippet", DogSize.Medium)),
                            List.of("White"),
                            List.of(),
                            List.of()
                    )
                )
        );
    }

    @Test
    void handleRequest_GET() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
                                .withPath("/")
                                .build())
                        .build())
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertNotNull(apiGatewayV2HTTPResponse.getBody());
        assertTrue(apiGatewayV2HTTPResponse.getBody().contains("<!DOCTYPE html>"));
    }

    @Test
    void handleRequest() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
                                .withPath("/photos")
                                .build())
                        .build())
                .withBody("test")
                .withQueryStringParameters(Map.of("caption", "red dress"))
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        assertTrue(apiGatewayV2HTTPResponse.getBody().startsWith("{\"photos\":["));
    }
    @Test
    void handleRequest_colours() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
                                .withPath("/photos")
                                .build())
                        .build())
                .withBody("test")
                .withQueryStringParameters(Map.of("colours", "White"))
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        String body = apiGatewayV2HTTPResponse.getBody();
        System.out.println(body);
        assertTrue(body.startsWith("{\"photos\":["));
    }
}