package io.rusleo.minirest.concurrency;

/**
 * Абстракция над исполнителем фоновых задач
 */
public interface TaskExecutor {

    /**
     * Отправить задачу на выполнение.
     */
    void execute(Runnable command);

    /**
     * Снять срез статистики по пулу.
     */
    WorkerStats stats();

    /**
     * Корректно остановить пул.
     */
    void shutdown();
}
