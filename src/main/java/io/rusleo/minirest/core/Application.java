package io.rusleo.minirest.core;

import io.rusleo.minirest.concurrency.WorkerPool;
import io.rusleo.minirest.http.Router;
import io.rusleo.minirest.http.SimpleHttpServer;
import io.rusleo.minirest.http.handlers.*;
import io.rusleo.minirest.metrics.MetricsRegistry;
import io.rusleo.minirest.tasks.TaskService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class Application {
    public static void main(String[] args) throws Exception {
        // Метрики (HTTP + задачи)
        MetricsRegistry metrics = new MetricsRegistry();

        // Пул рабочих задач (bounded queue + отказ при перегрузке)
        var worker = WorkerPool.createDefault(metrics);

        // Лёгкий пул под HttpServer (обработчики accept/read)
        Executor httpExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "http-acceptor");
            t.setDaemon(true);
            return t;
        });

        // Сервис задач (использует общий worker)
        TaskService tasks = new TaskService(worker, metrics);

        // Роутер и эндпоинты
        Router router = new Router(worker, metrics);
        router.get("/health", new HealthHandler());
        router.get("/metrics", new MetricsHandler(worker, metrics, tasks));
        router.get("/tasks/status", new TasksStatusHandler(tasks));
        router.get("/tasks/{id}", new TaskByIdHandler(tasks));
        router.post("/tasks/submit", new TasksSubmitHandler(tasks));

        // HTTP сервер
        SimpleHttpServer server = new SimpleHttpServer(
                new InetSocketAddress("0.0.0.0", 8080),
                httpExecutor,
                router);

        server.start();
        System.out.println("Server started on http://0.0.0.0:8080");
    }
}
