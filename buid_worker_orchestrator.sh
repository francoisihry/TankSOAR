#!/bin/sh
docker kill $(docker ps -aq)
docker rm $(docker ps -aq)
docker volume prune -f
docker network prune -f

mvn -f worker_orchestrator/pom.xml clean install -pl domain,infrastructure || { echo 'build worker_orchestrator failed' ; exit 1; }
docker build -f worker_orchestrator/infrastructure/src/main/docker/Dockerfile.jvm -t tanksoar/worker_orchestrator:latest-testing worker_orchestrator/infrastructure
mvn -f worker_orchestrator/pom.xml clean test -pl e2e || { echo 'e2e failed' ; exit 1; }
docker tag tanksoar/worker_orchestrator:latest-testing tanksoar/worker_orchestrator:latest
