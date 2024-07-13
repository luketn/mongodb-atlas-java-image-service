package com.mycodefu;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LocalRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);

        Main main = new Main();
        server.createContext("/", exchange -> {
            try {
                APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder eventBuilder = APIGatewayV2HTTPEvent.builder()
                        .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                                .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                        .withMethod(exchange.getRequestMethod())
                                        .build())
                                .build());

                //handle query string parameters
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    Map<String, String> queryParameters = parseQueryString(query);
                    eventBuilder.withQueryStringParameters(queryParameters);
                }

                byte[] body = exchange.getRequestBody().readAllBytes();
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    String base64EncodedBody = Base64.getEncoder().encodeToString(body);
                    eventBuilder.withBody(base64EncodedBody);
                    eventBuilder.withIsBase64Encoded(true);
                }
                APIGatewayV2HTTPEvent event = eventBuilder.build();

                APIGatewayV2HTTPResponse response = main.handleRequest(event, null);

                response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
                if (response.getBody().isEmpty()) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    byte[] bytes;
                    if (response.getIsBase64Encoded()) {
                        bytes = Base64.getDecoder().decode(response.getBody());
                    } else {
                        bytes = response.getBody().getBytes(StandardCharsets.UTF_8);
                    }
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                }
                exchange.close();
            } catch (IOException e) {
                System.out.println("Error handling request: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println(" Server started on http://localhost:8001");
    }


    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> queryParameters = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
            queryParameters.put(key, value);
        }
        return queryParameters;
    }
}
