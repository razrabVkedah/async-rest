package io.rusleo.minirest.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Обёртка над ThreadPoolExecutor, реализующая TaskExecutor.
 */
public final class PooledTaskExecutor implements TaskExecutor {

    private final ThreadPoolExecutor executor;

    public PooledTaskExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public WorkerStats stats() {
        return new WorkerStats(
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    public ExecutorService rawExecutor() {
        return executor;
    }
}
