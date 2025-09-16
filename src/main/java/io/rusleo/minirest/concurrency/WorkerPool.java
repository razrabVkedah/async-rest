package io.rusleo.minirest.concurrency;

import io.rusleo.minirest.metrics.MetricsRegistry;

import java.util.concurrent.*;

public final class WorkerPool {
    private WorkerPool() {
    }

    public static ThreadPoolExecutor createDefault(MetricsRegistry metrics) {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        int core = Math.min(cores, 4);
        int max = Math.max(core, cores * 4);

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1024);

        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "worker-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        };

        RejectedExecutionHandler rejection = (r, ex) -> {
            // Учёт отказов как перегрузка — метрика (не HTTP-ответ).
            metrics.markTaskRejected();
            throw new RejectedExecutionException("Worker queue full");
        };

        return new ThreadPoolExecutor(core, max, 60, TimeUnit.SECONDS, queue, tf, rejection);
    }
}
