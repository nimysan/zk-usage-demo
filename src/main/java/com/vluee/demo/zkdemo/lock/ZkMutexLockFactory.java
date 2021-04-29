package com.vluee.demo.zkdemo.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class ZkMutexLockFactory implements Watcher {

    private static Logger logger = LoggerFactory.getLogger(ZkMutexLockFactory.class);

    private ZooKeeper zk;
    private final static Long PART_UNIT = 1000L;

    public ZkMutexLockFactory() {
        try {
            zk = new ZooKeeper("localhost:2188", 5000, this);
            logger.info("Zk information: {}" + zk);
            Stat exists = zk.exists("/lock", false);
            if (exists == null) {
                zk.create("/lock", new byte[0], OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new RuntimeException("ZK setup error, please check.", e);
        }

    }

    public ZkMutexLock lock(String resourceName, long nanosTimeout) throws IOException, InterruptedException, KeeperException {
        try {
            if (nanosTimeout < 0) {
                return null;
            }
            logger.debug("Try to get lock for {}", resourceName);
            ZkMutexLock zkMutexLock = tryLock(resourceName);
            if (zkMutexLock != null) {
                return zkMutexLock;
            } else {
                zkMutexLock = spinAndLock(resourceName, nanosTimeout);
            }
            if (zkMutexLock == null) {
                throw new RuntimeException("Lock Failed for resource " + resourceName);
            }
            return zkMutexLock;
        } catch (Exception e) {
            throw new RuntimeException("Lock failed", e);
        }

    }

    /**
     * 在 nanosTimeout范围内自旋等待获取锁， 超时则获取锁失败
     *
     * @param resourceName
     * @param nanosTimeout
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    private ZkMutexLock spinAndLock(String resourceName, long nanosTimeout) throws InterruptedException, KeeperException {
        if (nanosTimeout < 0L) {
            return null;
        }
        long start = System.nanoTime();
        for (; ; ) {
            LockSupport.parkNanos(nanosTimeout < PART_UNIT ? nanosTimeout : PART_UNIT); // spin
            ZkMutexLock zkMutexLock = tryLock(resourceName);
            if (zkMutexLock != null) {
                return zkMutexLock;
            }
            if ((System.nanoTime() - start) > nanosTimeout) {
                return null;
            }
        }
    }


    private ZkMutexLock tryLock(String resourceName) throws InterruptedException, KeeperException {
        String path = "/lock/" + resourceName;

        Stat exists = zk.exists(path, false);

        //如何自旋等待，直到拿到锁？ TODO
        if (exists == null) {
            try {
                String s = zk.create(path, new byte[0], OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL);
                if (s.equals(path)) {
                    return new ZkMutexLock(path, this);
                }
            } catch (KeeperException keeperException) {

                if (KeeperException.Code.NODEEXISTS.equals(keeperException.code())) {
                    //在创建过程中，别的进程获得了该锁，获取锁失败
                    return null;
                }
                throw keeperException;
            }
        }
        return null;
    }

    public void unlock(String lockPath) throws InterruptedException, KeeperException {
        try {
            zk.delete(lockPath, 0);
            logger.debug("Try to release lock for {}", lockPath);
        } catch (Exception exception) {

        } finally {
            destroyZookeeperSession();
        }
    }

    private void destroyZookeeperSession() {
        if (this.zk != null) {
            try {
                this.zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.debug("---- {} ---- ", watchedEvent);
    }
}
