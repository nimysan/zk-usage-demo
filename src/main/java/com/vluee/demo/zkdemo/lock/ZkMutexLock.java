package com.vluee.demo.zkdemo.lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class ZkMutexLock implements Watcher {

    private final String lockPath;
    private final ZkMutexLockFactory lockFactory;

    public ZkMutexLock(String lockPath, ZkMutexLockFactory zkMutexLockFactory) {
        this.lockPath = lockPath;
        this.lockFactory = zkMutexLockFactory;
    }


    public void unlock() throws InterruptedException, KeeperException {
        lockFactory.unlock(lockPath);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
