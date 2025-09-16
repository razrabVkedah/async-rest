package io.rusleo.minirest.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface Route {
    void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException;
}
