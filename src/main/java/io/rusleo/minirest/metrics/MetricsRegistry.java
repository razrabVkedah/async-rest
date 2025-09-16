package io.rusleo.minirest.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public final class MetricsRegistry {
    // HTTP
    private final AtomicInteger httpInFlight = new AtomicInteger(0);
    private final LongAdder httpCompleted = new LongAdder();
    private final LongAdder httpTotalLatencyNanos = new LongAdder();

    // Tasks
    private final AtomicInteger tasksRunning = new AtomicInteger(0);
    private final LongAdder tasksCompleted = new LongAdder();
    private final LongAdder tasksFailed = new LongAdder();
    private final LongAdder tasksRejected = new LongAdder();

    // --- HTTP ---
    public void incHttpInFlight() {
        httpInFlight.incrementAndGet();
    }

    public void decHttpInFlight() {
        httpInFlight.decrementAndGet();
    }

    public void markHttpCompleted(long latencyNs) {
        httpCompleted.increment();
        httpTotalLatencyNanos.add(latencyNs);
    }

    public int getHttpInFlight() {
        return httpInFlight.get();
    }

    public long getHttpCompleted() {
        return httpCompleted.sum();
    }

    public long getHttpAvgLatencyMs() {
        long comp = httpCompleted.sum();
        return comp == 0 ? 0 : (httpTotalLatencyNanos.sum() / comp) / 1_000_000L;
    }

    // --- Tasks ---
    public void incTasksRunning() {
        tasksRunning.incrementAndGet();
    }

    public void decTasksRunning() {
        tasksRunning.decrementAndGet();
    }

    public void markTaskCompleted() {
        tasksCompleted.increment();
    }

    public void markTaskFailed() {
        tasksFailed.increment();
    }

    public void markTaskRejected() {
        tasksRejected.increment();
    }

    public int getTasksRunning() {
        return tasksRunning.get();
    }

    public long getTasksCompleted() {
        return tasksCompleted.sum();
    }

    public long getTasksFailed() {
        return tasksFailed.sum();
    }

    public long getTasksRejected() {
        return tasksRejected.sum();
    }
}
