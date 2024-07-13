package com.mycodefu;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void handleRequest_GET() {
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = new Main().handleRequest(APIGatewayV2HTTPEvent.builder()
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("GET")
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
                                .withMethod("POST")
                                .build())
                        .build())
                .withBody("test")
                .withIsBase64Encoded(false)
                .build(), null);

        assertNotNull(apiGatewayV2HTTPResponse);
        assertEquals(200, apiGatewayV2HTTPResponse.getStatusCode());
        assertFalse(apiGatewayV2HTTPResponse.getIsBase64Encoded());
        assertEquals("Hello", apiGatewayV2HTTPResponse.getBody());
    }
}