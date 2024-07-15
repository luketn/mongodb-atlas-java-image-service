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
        assertTrue(apiGatewayV2HTTPResponse.getBody().startsWith("{\"photos\": ["));
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
                .withQueryStringParameters(Map.of("colours", "Brown,Black"))
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        String body = apiGatewayV2HTTPResponse.getBody();
        assertTrue(body.startsWith("{\"photos\": ["));
        System.out.println(body);
    }
}