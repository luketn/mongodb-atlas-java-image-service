package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
        System.out.println("Received request:\n" + apiGatewayV2HTTPEvent);

        //return cacheable options
        String method = apiGatewayV2HTTPEvent.getRequestContext().getHttp().getMethod();
        switch(method) {
            case "POST": {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(200)
                        .withBody("Hello")
                        .withIsBase64Encoded(false)
                        .withHeaders(Map.of("Content-Type", "text/plain"))
                        .build();
            }
            case "GET": {
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