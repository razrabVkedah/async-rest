package io.rusleo.minirest.tasks;

import io.rusleo.minirest.metrics.MetricsRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public final class TaskService {
    private final ThreadPoolExecutor worker;
    private final MetricsRegistry metrics;
    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();

    public TaskService(ThreadPoolExecutor worker, MetricsRegistry metrics) {
        this.worker = worker;
        this.metrics = metrics;
    }

    public String submitDelay(long millis) {
        String id = UUID.randomUUID().toString();
        Task task = new Task(id, "delay");
        tasks.put(id, task);

        try {
            worker.execute(() -> {
                metrics.incTasksRunning();
                try {
                    task.markRunning();
                    Thread.sleep(Math.max(0, millis));
                    task.markCompleted();
                    metrics.markTaskCompleted();
                } catch (Throwable t) {
                    task.markFailed(t.getClass().getSimpleName() + ": " + t.getMessage());
                    metrics.markTaskFailed();
                } finally {
                    metrics.decTasksRunning();
                }
            });
        } catch (RejectedExecutionException rex) {
            task.markFailed("Rejected: queue full");
            metrics.markTaskRejected();
        }
        return id;
    }

    public Optional<Task> get(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Map<String, Task> snapshotAll() {
        return Map.copyOf(tasks);
    }
}
