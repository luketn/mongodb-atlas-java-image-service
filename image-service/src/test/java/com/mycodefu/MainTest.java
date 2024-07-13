package com.mycodefu;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
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
        assertEquals("{\"photos\": [" +
                "{\"caption\": \"The image shows two dogs sitting on a wooden deck. The dog on the left is a brown dog wearing a pink party hat and a pink dress. The other dog is a black and white spotted dog. Both dogs are wearing colorful party hats and dresses. The deck is surrounded by trees and there is a wooden railing in the background. The sky is blue and the weather appears to be sunny.\", " +
                "\"url\": \"https://image-search.mycodefu.com/photos/Images/n02102040-English_springer/n02102040_1259.jpg\"}, " +
                "{\"caption\": \"The image shows a small gray and black Pomeranian puppy sitting on a wooden floor next to a white teddy bear. The puppy is wearing a red and green floral dress with a white lace trim. Above the puppy, there is a speech bubble that reads \\\"I want my TEDDY!\\\" The puppy appears to be looking at the camera with a curious expression.\", " +
                "\"url\": \"https://image-search.mycodefu.com/photos/Images/n02112018-Pomeranian/n02112018_249.jpg\"}, " +
                "{\"caption\": \"The image shows a large black Newfoundland dog sitting on a grassy lawn in front of a wooden house. The dog is wearing a light blue bib with the words \\\"Dig Me\\\" and a cartoon character on it. There are two young girls on either side of the dog, one wearing a pink dress and the other wearing a blue dress. The girl in the pink dress is petting the dog's collar. The other girl is looking at the dog with a curious expression.\", " +
                "\"url\": \"https://image-search.mycodefu.com/photos/Images/n02111277-Newfoundland/n02111277_413.jpg\"}, " +
                "{\"caption\": \"The image is of a small pug dog wearing a black and white checkered dress. The dog is standing on a beige carpeted floor and is looking to the side with a curious expression on its face. The dress has a collar with a yellow tag attached to it, and the dog is holding a small toy in its mouth.\", " +
                "\"url\": \"https://image-search.mycodefu.com/photos/Images/n02086079-Pekinese/n02086079_186.jpg\"}, " +
                "{\"caption\": \"The image shows a woman walking a dog on a runway. The woman is wearing a red and gold dress with a black skirt and black heels. She is holding a red leash attached to the dog's collar. The dog is a white and brown borzoi with a red blanket draped over its body. The background of the runway is decorated with gold accents and there is a large tree on the right side of the image. The audience can be seen in the background.\", " +
                "\"url\": \"https://image-search.mycodefu.com/photos/Images/n02090622-borzoi/n02090622_8145.jpg\"}" +
        "]}", apiGatewayV2HTTPResponse.getBody());
    }
}