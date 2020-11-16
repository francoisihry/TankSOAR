package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
public class JvmBasedWorkerLockMechanism implements WorkerLockMechanism {

    private final Lock lock = new ReentrantLock();

    @Override
    public void lock(final WorkerId workerId) {
        lock.lock();
    }

    @Override
    public void unlock(final WorkerId workerId) {
        lock.unlock();
    }

}
