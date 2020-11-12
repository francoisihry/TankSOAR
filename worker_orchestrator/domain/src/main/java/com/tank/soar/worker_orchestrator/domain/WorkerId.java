package com.tank.soar.worker_orchestrator.domain;

// Use a class because equality needs in my use case ...

import java.util.Objects;

public final class WorkerId {

    private final String id;

    public WorkerId(final String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerId)) return false;
        WorkerId workerId = (WorkerId) o;
        return Objects.equals(id, workerId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WorkerId{" +
                "id='" + id + '\'' +
                '}';
    }
}
