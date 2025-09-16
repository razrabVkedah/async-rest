package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.tasks.Task;
import io.rusleo.minirest.tasks.TaskService;
import io.rusleo.minirest.tasks.TaskStatus;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

public final class TasksStatusHandler implements Route {
    private final TaskService tasks;

    public TasksStatusHandler(TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        Map<String, Task> all = tasks.snapshotAll();
        EnumMap<TaskStatus, Integer> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) counts.put(s, 0);
        for (Task t : all.values()) counts.put(t.status(), counts.get(t.status()) + 1);

        String json = "{"
                + "\"running\":" + counts.get(TaskStatus.RUNNING) + ","
                + "\"submitted\":" + counts.get(TaskStatus.SUBMITTED) + ","
                + "\"completed\":" + counts.get(TaskStatus.COMPLETED) + ","
                + "\"failed\":" + counts.get(TaskStatus.FAILED) + ","
                + "\"total\":" + all.size()
                + "}";
        sendJson(exchange, 200, json);
    }
}
