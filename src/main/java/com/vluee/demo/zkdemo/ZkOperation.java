package com.vluee.demo.zkdemo;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;


public class ZkOperation implements Watcher {
    private static final Logger logger = LoggerFactory.getLogger(ZkOperation.class);

    private ZooKeeper zk;

    public ZkOperation() {
        try {
            zk = new ZooKeeper("localhost:2188", 30000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test() throws InterruptedException, KeeperException {
        zk.create("/test", new byte[0], OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        zk.create("/test/lock", new byte[0], OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
//        zk.

//        zk.create("/test/lock", new byte[0], OPEN_ACL_UNSAFE,
//                CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("test");
    }

    public static void main(String[] args) throws InterruptedException, KeeperException {
        ZkOperation zkOperation = new ZkOperation();
        zkOperation.test();
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("Zk event {}", event);
    }
}
