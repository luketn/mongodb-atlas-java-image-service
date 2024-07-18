package com.mycodefu;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mycodefu.atlas.MongoConnection;
import com.mycodefu.data.*;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.AtlasMongoDBTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.mycodefu.util.Serializer.fromJson;
import static org.junit.jupiter.api.Assertions.*;

class MainTest extends AtlasMongoDBTest {

    @BeforeAll
    static void setup() throws IOException {
        MongoConnection.setConnectionString(connectionString());
        String indexResource = Files.readString(Paths.get("create-index.json"));
        MongoConnection.createAtlasIndex(
                "ImageSearch",
                "photo",
                PhotoFull.class,
                "default",
                BsonDocument.parse(indexResource),
                List.of(
                    new PhotoFull("Two white whippets standing in a grassy area with trees in the background.",
                            "The image shows two greyhounds standing side by side on a grassy field. The dog on the left is wearing a red bandana with white stars on it, while the one on the right is also wearing a white bandana. Both dogs are looking towards the camera with their ears perked up and their eyes focused on something in the distance. The background shows trees and a fence, suggesting that the photo was taken in a park or garden.",
                            "https://image-search.mycodefu.com/photos/Images/n02091134-whippet/n02091134_19308.jpg",
                            false,
                            List.of(
                                    new Dog(List.of("White"), "Whippet", DogSize.Medium),
                                    new Dog(List.of("White"), "Whippet", DogSize.Small)
                            )
                    ),
                    new PhotoFull("A standard schnauzer in the snow",
                            "The image is a close-up of a dog standing in the snow. The dog appears to be a miniature schnauzer, with a brown and white coat. It has a black nose and is looking directly at the camera with a curious expression. Its ears are perked up and its eyes are dark and alert. The snow is covering the ground and the dog's fur is wet, indicating that it has recently rained. There is a small orange tag around its neck. The background is blurred, but it seems to be an outdoor setting with trees and bushes.",
                            "https://image-search.mycodefu.com/photos/Images/n02097209-standard_schnauzer/n02097209_2629.jpg",
                            false,
                            List.of(
                                    new Dog(List.of("Brown", "Black"), "Standard Schnauzer", DogSize.Medium)
                            )
                    )
                )
        );
    }

    @Test
    void handleRequest_get_index_html() {
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
    void handleRequest_get_photos_snow() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
                                .withPath("/photos")
                                .build())
                        .build())
                .withQueryStringParameters(Map.of(
                        "caption", "snow"
                ))
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        String body = apiGatewayV2HTTPResponse.getBody();
        PhotoResults result = fromJson(body, PhotoResults.class);
        assertEquals(1, result.photos().size());
        assertEquals("A standard schnauzer in the snow", result.photos().get(0).summary());
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
                .withQueryStringParameters(Map.of("colours", "White"))
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        String body = apiGatewayV2HTTPResponse.getBody();
        PhotoResults result = fromJson(body, PhotoResults.class);
        assertEquals(1, result.photos().size());
        assertEquals("Two white whippets standing in a grassy area with trees in the background.", result.photos().get(0).summary());
    }
    @Test
    void handleRequest_no_query() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
                                .withPath("/photos")
                                .build())
                        .build())
                .withQueryStringParameters(Map.of())
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        String body = apiGatewayV2HTTPResponse.getBody();
        PhotoResults result = fromJson(body, PhotoResults.class);
        assertEquals(2, result.photos().size());
    }
}