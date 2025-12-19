package io.rusleo.minirest.concurrency;

public final class WorkerStats {
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final int activeCount;
    private final int queueSize;
    private final long completedTaskCount;

    public WorkerStats(int corePoolSize,
                       int maximumPoolSize,
                       int activeCount,
                       int queueSize,
                       long completedTaskCount) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.activeCount = activeCount;
        this.queueSize = queueSize;
        this.completedTaskCount = completedTaskCount;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }
}
