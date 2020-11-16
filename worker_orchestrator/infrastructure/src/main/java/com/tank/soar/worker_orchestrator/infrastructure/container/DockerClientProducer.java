package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class DockerClientProducer {

    @ConfigProperty(name = "docker.daemon.tcp.host")
    String host;

    @ConfigProperty(name = "docker.daemon.tcp.port")
    Integer port;

    @ApplicationScoped
    @Produces
    public DockerClient produceDockerClient() {
        final String url = String.format("tcp://%s:%d", host, port);
        final DockerClientConfig dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(url)
                .withDockerTlsVerify(false)
                .build();
        return DockerClientBuilder.getInstance(dockerClientConfig).build();
    }

}
