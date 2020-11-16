package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.domain.WorkerContainerManager;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListWorkersUseCase implements UseCase<ListWorkersCommand, List<Worker>> {

    // Dans ce cas d'utilisation je dois merger les containers et la base de données ...
    // priorité sur le container vs database ...
    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;

    public ListWorkersUseCase(final WorkerContainerManager workerContainerManager,
                              final WorkerRepository workerRepository) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
    }

    @Override
    public List<Worker> execute(final ListWorkersCommand command) {
        final List<? extends Worker> workersContainers = workerContainerManager.listAllContainers();
        final List<? extends Worker> workersDatabases = workerRepository.listAllWorkers();
        final Map<WorkerId, List<Worker>> workersByWorkerIds = Stream.concat(
                workersContainers.stream(), workersDatabases.stream())
                .collect(Collectors.groupingBy(Worker::workerId));
        return workersByWorkerIds.entrySet()
                .stream()
                .map(workerIdListEntry -> workerIdListEntry.getValue().stream()
                        .sorted(Comparator.comparing(worker -> worker.lastUpdateStateDate()))
                        .reduce((first, second) -> second).get())
                .collect(Collectors.toList());
    }

}
