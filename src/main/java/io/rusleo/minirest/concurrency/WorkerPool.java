package io.rusleo.minirest.concurrency;

import java.util.concurrent.*;

/**
 * Фабрика дефолтного пула рабочих потоков для фоновых задач.
 */
public final class WorkerPool {
    private WorkerPool() {
    }

    /**
     * Создаёт дефолтный TaskExecutor для фоновых задач.
     */
    public static TaskExecutor createDefault() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        int core = Math.max(4, cores);
        int max  = cores * 8;

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1024);

        ThreadFactory tf = r -> new Thread(r, "worker-" + System.nanoTime());

        RejectedExecutionHandler rejection = (r, ex) -> {
            throw new RejectedExecutionException("Worker queue full");
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core,
                max,
                60,
                TimeUnit.SECONDS,
                queue,
                tf,
                rejection
        );

        return new PooledTaskExecutor(executor);
    }
}
