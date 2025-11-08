package io.rusleo.minirest.core;

import io.rusleo.minirest.concurrency.WorkerPool;
import io.rusleo.minirest.http.Router;
import io.rusleo.minirest.http.SimpleHttpServer;
import io.rusleo.minirest.http.handlers.*;
import io.rusleo.minirest.metrics.MetricsRegistry;
import io.rusleo.minirest.tasks.TaskService;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public final class Application {
    public static void main(String[] args) throws Exception {
        MetricsRegistry metrics = new MetricsRegistry();

        // 1) Отдельный пул для фоновых задач
        ThreadPoolExecutor taskWorker = WorkerPool.createDefault(metrics);

        // 2) Отдельный пул для HTTP-обработчиков
        ThreadFactory httpTf = r -> {
            Thread t = new Thread(r, "http-handler");
            t.setDaemon(true);
            return t;
        };
        ThreadPoolExecutor httpExecutor = new ThreadPoolExecutor(
                Math.max(4, Runtime.getRuntime().availableProcessors()),
                Math.max(8, Runtime.getRuntime().availableProcessors() * 2),
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(512),
                httpTf,
                new ThreadPoolExecutor.AbortPolicy()
        );

        // Сервис задач использует ТОЛЬКО taskWorker
        TaskService tasks = new TaskService(taskWorker, metrics);

        Router router = new Router(metrics);
        router.get("/health", new HealthHandler());
        router.get("/metrics", new MetricsHandler(taskWorker, metrics, tasks));
        router.get("/tasks/status", new TasksStatusHandler(tasks));
        router.get("/tasks/{id}", new TaskByIdHandler(tasks));
        router.post("/tasks/submit", new TasksSubmitHandler(tasks));
        router.post("/tasks/many", new ManyTasksSubmitHandler(tasks));

        SimpleHttpServer server = new SimpleHttpServer(
                new InetSocketAddress("0.0.0.0", 8080),
                httpExecutor,
                router
        );

        server.start();
        System.out.println("Server started on http://0.0.0.0:8080");
    }
}
