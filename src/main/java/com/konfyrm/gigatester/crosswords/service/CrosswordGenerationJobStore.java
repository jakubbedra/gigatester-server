package com.konfyrm.gigatester.crosswords.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CrosswordGenerationJobStore {

    public enum Status { PENDING, DONE, FAILED, CANCELLED }

    public record JobResult(Status status, UUID stateId, String error) {}

    private final ConcurrentHashMap<UUID, JobResult> jobs = new ConcurrentHashMap<>();

    public UUID create() {
        UUID jobId = UUID.randomUUID();
        jobs.put(jobId, new JobResult(Status.PENDING, null, null));
        return jobId;
    }

    public void complete(UUID jobId, UUID stateId) {
        jobs.put(jobId, new JobResult(Status.DONE, stateId, null));
    }

    public void fail(UUID jobId, String error) {
        jobs.put(jobId, new JobResult(Status.FAILED, null, error));
    }

    public Optional<JobResult> get(UUID jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public boolean cancel(UUID jobId) {
        return jobs.computeIfPresent(jobId, (id, existing) ->
                existing.status() == Status.PENDING
                        ? new JobResult(Status.CANCELLED, null, "Cancelled by user")
                        : existing
        ) != null;
    }

    public boolean isCancelled(UUID jobId) {
        JobResult r = jobs.get(jobId);
        return r != null && r.status() == Status.CANCELLED;
    }
}
