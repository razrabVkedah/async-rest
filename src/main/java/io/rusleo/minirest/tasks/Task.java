package io.rusleo.minirest.tasks;

import java.time.Instant;
import java.util.Objects;

public final class Task {
    private final String id;
    private final String type;
    private final long createdAtEpochMs;

    private volatile TaskStatus status = TaskStatus.SUBMITTED;
    private volatile Long startedAtEpochMs;
    private volatile Long finishedAtEpochMs;
    private volatile String error;

    public Task(String id, String type) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.createdAtEpochMs = Instant.now().toEpochMilli();
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public TaskStatus status() {
        return status;
    }

    public long createdAtEpochMs() {
        return createdAtEpochMs;
    }

    public Long startedAtEpochMs() {
        return startedAtEpochMs;
    }

    public Long finishedAtEpochMs() {
        return finishedAtEpochMs;
    }

    public String error() {
        return error;
    }

    public void markRunning() {
        status = TaskStatus.RUNNING;
        startedAtEpochMs = Instant.now().toEpochMilli();
    }

    public void markCompleted() {
        status = TaskStatus.COMPLETED;
        finishedAtEpochMs = Instant.now().toEpochMilli();
    }

    public void markFailed(String err) {
        status = TaskStatus.FAILED;
        finishedAtEpochMs = Instant.now().toEpochMilli();
        error = err;
    }
}
