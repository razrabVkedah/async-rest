package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.util.QueryParams;
import io.rusleo.minirest.util.Strings;

import java.io.IOException;
import java.util.Map;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

public final class TasksSubmitHandler implements Route {
    private final io.rusleo.minirest.tasks.TaskService tasks;

    public TasksSubmitHandler(io.rusleo.minirest.tasks.TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        Map<String, String> q = QueryParams.parse(exchange.getRequestURI().getQuery());
        String type = q.getOrDefault("type", "delay");
        if (!"delay".equals(type)) {
            sendJson(exchange, 400, "{\"error\":\"unsupported_type\"}");
            return;
        }
        long ms = Strings.parseLongOr(q.getOrDefault("ms", "200"), 200);
        String id = tasks.submitDelay(ms);
        String json = "{"
                + "\"taskId\":\"" + id + "\","
                + "\"statusUrl\":\"/tasks/" + id + "\""
                + "}";
        // 202 Accepted — задача принята
        sendJson(exchange, 202, json);
    }
}
