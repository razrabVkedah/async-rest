package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.util.QueryParams;
import io.rusleo.minirest.util.Strings;

import java.io.IOException;
import java.util.Map;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

public final class ManyTasksSubmitHandler implements Route {
    private final io.rusleo.minirest.tasks.TaskService tasks;

    public ManyTasksSubmitHandler(io.rusleo.minirest.tasks.TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        Map<String, String> q = QueryParams.parse(exchange.getRequestURI().getQuery());

        // Параметры: количество задач и диапазон задержек
        long countLong = Strings.parseLongOr(q.getOrDefault("count", "10"), 10);
        int count = (int) Math.max(1, Math.min(countLong, 10_000));

        long minMs = Strings.parseLongOr(q.getOrDefault("minMs", "200"), 200);
        long maxMs = Strings.parseLongOr(q.getOrDefault("maxMs", "2000"), 2000);
        if (minMs > maxMs) {
            long tmp = minMs; minMs = maxMs; maxMs = tmp;
        }

        java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();

        StringBuilder sb = new StringBuilder(128 + count * 64);
        sb.append('{')
                .append("\"count\":").append(count).append(',')
                .append("\"minMs\":").append(minMs).append(',')
                .append("\"maxMs\":").append(maxMs).append(',')
                .append("\"tasks\":[");

        for (int i = 0; i < count; i++) {
            long delay = (minMs == maxMs) ? minMs : rnd.nextLong(minMs, maxMs + 1);
            String id = tasks.submitDelay(delay);

            if (i > 0) sb.append(',');
            sb.append('{')
                    .append("\"taskId\":\"").append(id).append("\",")
                    .append("\"delayMs\":").append(delay).append(',')
                    .append("\"statusUrl\":\"/tasks/").append(id).append("\"")
                    .append('}');
        }
        sb.append("]}");

        sendJson(exchange, 202, sb.toString());
    }
}
