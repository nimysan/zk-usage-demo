package com.vluee.demo.zkdemo.lock;

import org.apache.zookeeper.KeeperException;

public class ZkMutexLock {

    private final String lockPath;
    private final ZkMutexLockFactory lockFactory;

    public ZkMutexLock(String lockPath, ZkMutexLockFactory zkMutexLockFactory) {
        this.lockPath = lockPath;
        this.lockFactory = zkMutexLockFactory;
    }


    public void unlock() throws InterruptedException, KeeperException {
        lockFactory.unlock(lockPath);
    }
    
}
