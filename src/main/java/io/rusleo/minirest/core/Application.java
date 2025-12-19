package io.rusleo.minirest.core;

import io.rusleo.minirest.concurrency.TaskExecutor;
import io.rusleo.minirest.concurrency.WorkerPool;
import io.rusleo.minirest.http.Router;
import io.rusleo.minirest.http.SimpleHttpServer;
import io.rusleo.minirest.http.handlers.*;
import io.rusleo.minirest.metrics.MetricsRegistry;
import io.rusleo.minirest.tasks.TaskService;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class Application {

    public static void main(String[] args) throws Exception {
        MetricsRegistry metrics = new MetricsRegistry();

        // 1) Пул для фоновых задач (delay и т.п.)
        TaskExecutor taskExecutor = WorkerPool.createDefault();

        // 2) Отдельный пул потоков для HTTP-обработчиков.
        ThreadFactory httpTf = r -> new Thread(r, "http-handler");

        // кэшированный пул для HTTP.
        ExecutorService httpExecutor = Executors.newCachedThreadPool(httpTf);

        TaskService tasks = new TaskService(taskExecutor, metrics);

        Router router = new Router(metrics);
        router.get("/health", new HealthHandler());
        router.get("/metrics", new MetricsHandler(taskExecutor, metrics, tasks));
        router.get("/tasks/status", new TasksStatusHandler(tasks));
        router.get("/tasks/{id}", new TaskByIdHandler(tasks));
        router.post("/tasks/submit", new TasksSubmitHandler(tasks));
        router.post("/tasks/many", new ManyTasksSubmitHandler(tasks));

        SimpleHttpServer server = new SimpleHttpServer(
                new InetSocketAddress("0.0.0.0", 8080),
                httpExecutor,
                router
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            taskExecutor.shutdown();
            httpExecutor.shutdown();
        }, "shutdown-hook"));

        server.start();
        System.out.println("Server started on http://0.0.0.0:8080");
    }
}
