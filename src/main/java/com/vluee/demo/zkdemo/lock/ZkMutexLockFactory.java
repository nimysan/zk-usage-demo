package com.vluee.demo.zkdemo.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class ZkMutexLockFactory implements Watcher {


    ZooKeeper zk;

    public ZkMutexLock lock(String resourceName) throws IOException, InterruptedException, KeeperException {
        if (zk == null) {
            zk = new ZooKeeper("localhost:2188", 3000, this);
            if (zk.exists("/lock", false) == null) {
                zk.create("/lock", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        }
        String path = "/lock/" + resourceName;
        Stat exists = zk.exists(path, false);

        //如何自旋等待，直到拿到锁？ TODO
        if (exists == null) {
            String s = zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            if (s.equals(path)) {
                return new ZkMutexLock(path, this);
            }
        }
        return null;
    }

    public void unlock(String lockPath) throws InterruptedException, KeeperException {
        zk.delete(lockPath, 0);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
