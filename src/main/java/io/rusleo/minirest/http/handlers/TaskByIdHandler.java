package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.tasks.Task;
import io.rusleo.minirest.tasks.TaskService;
import io.rusleo.minirest.util.Json;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

public final class TaskByIdHandler implements Route {
    private final TaskService tasks;

    public TaskByIdHandler(TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        Optional<Task> opt = tasks.get(id);
        if (opt.isEmpty()) {
            sendJson(exchange, 404, "{\"error\":\"not_found\"}");
            return;
        }
        Task t = opt.get();
        String json = "{"
                + "\"id\":" + Json.esc(t.id()) + ","
                + "\"type\":" + Json.esc(t.type()) + ","
                + "\"status\":" + Json.esc(t.status().name()) + ","
                + "\"createdAt\":" + t.createdAtEpochMs() + ","
                + "\"startedAt\":" + (t.startedAtEpochMs() == null ? "null" : t.startedAtEpochMs()) + ","
                + "\"finishedAt\":" + (t.finishedAtEpochMs() == null ? "null" : t.finishedAtEpochMs()) + ","
                + "\"error\":" + (t.error() == null ? "null" : Json.esc(t.error()))
                + "}";
        sendJson(exchange, 200, json);
    }
}
