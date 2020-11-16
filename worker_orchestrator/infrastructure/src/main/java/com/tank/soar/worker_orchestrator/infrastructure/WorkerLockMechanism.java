package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

// TODO should use the workerId to avoid locking for everyone
// Remark: only works on a single jvm mode. If you want multiple instances of this application running: use Hazelcast
public interface WorkerLockMechanism {

    void lock(WorkerId workerId);

    void unlock(WorkerId workerId);

}
