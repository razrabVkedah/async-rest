package io.rusleo.minirest.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.rusleo.minirest.concurrency.TaskExecutor;
import io.rusleo.minirest.concurrency.WorkerStats;
import io.rusleo.minirest.http.Route;
import io.rusleo.minirest.metrics.MetricsRegistry;
import io.rusleo.minirest.tasks.TaskService;

import java.io.IOException;
import java.util.Map;

import static io.rusleo.minirest.http.HttpExchangeHelper.sendJson;

/**
 * HTTP-эндпоинт для просмотра метрик по HTTP и фоновой очереди задач.
 */
public final class MetricsHandler implements Route {
    private final TaskExecutor taskExecutor;
    private final MetricsRegistry metrics;
    private final TaskService tasks;

    public MetricsHandler(TaskExecutor taskExecutor, MetricsRegistry metrics, TaskService tasks) {
        this.taskExecutor = taskExecutor;
        this.metrics = metrics;
        this.tasks = tasks;
    }

    @Override
    public void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        WorkerStats ws = taskExecutor.stats();

        String json = "{"
                + "\"http\":{"
                + "\"in_flight\":" + metrics.getHttpInFlight() + ","
                + "\"completed\":" + metrics.getHttpCompleted() + ","
                + "\"avg_latency_ms\":" + metrics.getHttpAvgLatencyMs()
                + "},"
                + "\"worker\":{"
                + "\"core\":" + ws.getCorePoolSize() + ","
                + "\"max\":" + ws.getMaximumPoolSize() + ","
                + "\"active\":" + ws.getActiveCount() + ","
                + "\"queue\":" + ws.getQueueSize() + ","
                + "\"completed\":" + ws.getCompletedTaskCount()
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
