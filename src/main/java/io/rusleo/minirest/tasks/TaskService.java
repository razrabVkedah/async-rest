package io.rusleo.minirest.tasks;

import io.rusleo.minirest.concurrency.TaskExecutor;
import io.rusleo.minirest.metrics.MetricsRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;

/**
 * Сервис управления задачами.
 * Не зависит от конкретной реализации пула потоков, работает через TaskExecutor.
 */
public final class TaskService {
    private final TaskExecutor worker;
    private final MetricsRegistry metrics;
    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();

    public TaskService(TaskExecutor worker, MetricsRegistry metrics) {
        this.worker = worker;
        this.metrics = metrics;
    }

    /**
     * Создаёт задачу типа "delay" с задержкой millis.
     * Возвращает ID задачи (даже если очередь была переполнена —
     * в этом случае статус будет FAILED и error = "Rejected: queue full").
     */
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
            // Очередь пула переполнена — помечаем задачу как проваленную.
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
