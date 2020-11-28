package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;

import java.util.Objects;

public final class NewWorkerEntityEvent implements WorkerEvent {

    private final WorkerId workerId;
    private final EventType eventType;
    private final UTCZonedDateTime eventDate;
    private final UserEventType userEventType;

    private NewWorkerEntityEvent(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.eventType = Objects.requireNonNull(builder.eventType);
        this.eventDate = Objects.requireNonNull(builder.eventDate);
        this.userEventType = Objects.requireNonNull(builder.userEventType);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public WorkerId workerId() {
        return workerId;
    }

    @Override
    public EventType eventType() {
        return eventType;
    }

    @Override
    public UTCZonedDateTime eventDate() {
        return eventDate;
    }

    @Override
    public UserEventType userEventType() {
        return userEventType;
    }

    @Override
    public DockerContainerStatus dockerContainerStatus() {
        throw new UnsupportedOperationException("It is an user event !");
    }

    public static final class Builder {
        private WorkerId workerId;
        private EventType eventType;
        private UTCZonedDateTime eventDate;
        private UserEventType userEventType;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withEventType(final EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withEventDate(final UTCZonedDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public Builder withUserEventType(final UserEventType userEventType) {
            this.userEventType = userEventType;
            return this;
        }

        public NewWorkerEntityEvent build() {
            return new NewWorkerEntityEvent(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewWorkerEntityEvent)) return false;
        NewWorkerEntityEvent that = (NewWorkerEntityEvent) o;
        return Objects.equals(workerId, that.workerId) &&
                eventType == that.eventType &&
                Objects.equals(eventDate, that.eventDate) &&
                userEventType == that.userEventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, eventType, eventDate, userEventType);
    }

    @Override
    public String toString() {
        return "NewWorkerUserEvent{" +
                "workerId=" + workerId +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                ", userEventType=" + userEventType +
                '}';
    }
}
