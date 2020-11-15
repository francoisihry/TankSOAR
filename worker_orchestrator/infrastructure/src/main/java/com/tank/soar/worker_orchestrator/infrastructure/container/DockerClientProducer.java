package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class DockerClientProducer {

    @ApplicationScoped
    @Produces
    public DockerClient produceDockerClient() {
        final DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        final DockerHttpClient dockerHttpClient = new OkDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        return DockerClientBuilder.getInstance(dockerClientConfig).build();
    }

}
