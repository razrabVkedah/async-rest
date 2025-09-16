package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

import java.io.IOException;
import java.util.Map;

public final class HealthHandler implements Route {
    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        sendJson(exchange, 200, "{\"status\":\"ok\"}");
    }
}
