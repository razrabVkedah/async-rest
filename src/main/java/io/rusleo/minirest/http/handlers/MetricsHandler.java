package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.metrics.MetricsRegistry;
import io.rusleo.minirest.tasks.TaskService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

public final class MetricsHandler implements Route {
    private final ThreadPoolExecutor worker;
    private final MetricsRegistry metrics;
    private final TaskService tasks;

    public MetricsHandler(ThreadPoolExecutor worker, MetricsRegistry metrics, TaskService tasks) {
        this.worker = worker;
        this.metrics = metrics;
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String json = "{"
                + "\"http\":{"
                + "\"in_flight\":" + metrics.getHttpInFlight() + ","
                + "\"completed\":" + metrics.getHttpCompleted() + ","
                + "\"avg_latency_ms\":" + metrics.getHttpAvgLatencyMs()
                + "},"
                + "\"worker\":{"
                + "\"core\":" + worker.getCorePoolSize() + ","
                + "\"max\":" + worker.getMaximumPoolSize() + ","
                + "\"active\":" + worker.getActiveCount() + ","
                + "\"queue\":" + worker.getQueue().size() + ","
                + "\"completed\":" + worker.getCompletedTaskCount()
                + "},"
                + "\"tasks\":{"
                + "\"running\":" + metrics.getTasksRunning() + ","
                + "\"completed\":" + metrics.getTasksCompleted() + ","
                + "\"failed\":" + metrics.getTasksFailed() + ","
                + "\"rejected\":" + metrics.getTasksRejected()
                + "}"
                + "}";
        sendJson(exchange, 200, json);
    }
}
