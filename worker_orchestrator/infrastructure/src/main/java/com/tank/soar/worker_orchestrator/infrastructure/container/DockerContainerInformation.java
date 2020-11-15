package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.ContainerInformation;

import java.util.Objects;

public final class DockerContainerInformation implements ContainerInformation {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String informationAsJsonRepresentation;

    public DockerContainerInformation(final InspectContainerResponse inspectContainerResponse) {
        try {
            this.informationAsJsonRepresentation = OBJECT_MAPPER.writeValueAsString(inspectContainerResponse);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String fullInformation() {
        return informationAsJsonRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerContainerInformation)) return false;
        DockerContainerInformation that = (DockerContainerInformation) o;
        return Objects.equals(informationAsJsonRepresentation, that.informationAsJsonRepresentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(informationAsJsonRepresentation);
    }

    @Override
    public String toString() {
        return "DockerContainerInformation{" +
                "informationAsJsonRepresentation='" + informationAsJsonRepresentation + '\'' +
                '}';
    }
}
