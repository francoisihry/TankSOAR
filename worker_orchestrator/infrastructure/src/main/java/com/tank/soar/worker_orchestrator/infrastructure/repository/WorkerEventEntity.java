package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public final class WorkerEventEntity {

    private final WorkerId workerId;
    private final EventType eventType;
    private final UTCZonedDateTime eventDate;
    private final DockerContainerStatus dockerContainerStatus;
    private final UserEventType userEventType;

    public WorkerEventEntity(final WorkerId workerId,
                             final EventType eventType,
                             final UTCZonedDateTime eventDate,
                             final DockerContainerStatus dockerContainerStatus,
                             final UserEventType userEventType) {
        this.workerId = Objects.requireNonNull(workerId);
        this.eventType = Objects.requireNonNull(eventType);
        this.eventDate = Objects.requireNonNull(eventDate);
        this.dockerContainerStatus = dockerContainerStatus;
        this.userEventType = userEventType;
    }

    public UTCZonedDateTime eventDate() {
        return eventDate;
    }

    public WorkerStatus workerStatus() {
        switch (eventType) {
            case USER:
                switch (userEventType) {
                    case CREATION_REQUESTED:
                        return WorkerStatus.CREATION_REQUESTED;
                    default:
                        throw new IllegalStateException("should not be here");
                }
            case DOCKER:
                return dockerContainerStatus.toWorkerStatus();
            default:
                throw new IllegalStateException("should not be here");
        }
    }

    public WorkerEventEntity(final ResultSet resultSet) throws SQLException {
        this(
                new WorkerId(resultSet.getString("workerEventWorkerId")),
                EventType.valueOf(resultSet.getString("workerEventEventType")),
                UTCZonedDateTime.of(resultSet.getObject("workerEventEventDate", LocalDateTime.class),
                        resultSet.getString("workerEventZoneOffset")),
                Optional.ofNullable(resultSet.getString("workerEventDockerStatus")).map(DockerContainerStatus::fromDockerStatus).orElse(null),
                Optional.ofNullable(resultSet.getString("workerEventUserEventType")).map(UserEventType::valueOf).orElse(null)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerEventEntity)) return false;
        WorkerEventEntity that = (WorkerEventEntity) o;
        return Objects.equals(workerId, that.workerId) &&
                eventType == that.eventType &&
                Objects.equals(eventDate, that.eventDate) &&
                dockerContainerStatus == that.dockerContainerStatus &&
                userEventType == that.userEventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, eventType, eventDate, dockerContainerStatus, userEventType);
    }

    @Override
    public String toString() {
        return "WorkerEventEntity{" +
                "workerId=" + workerId +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                ", dockerContainerStatus=" + dockerContainerStatus +
                ", userEventType=" + userEventType +
                '}';
    }
}
