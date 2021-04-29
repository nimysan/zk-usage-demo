package com.vluee.demo.zkdemo.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 模拟分布式环境下一次只允许一个进程处理某个数据
 */
public class ZkMutexLockUsage {
    private static Logger logger = LoggerFactory.getLogger(ZkMutexLockUsage.class);

    private static CountDownLatch latch;

    private static int shareData = 0;
    private final static String RESOURCE_NAME = "DEMO_SHARE_DATA";
    private static List<Integer> history = new ArrayList<>();


    private static boolean testWithLock = false;
    private static int threadSize = 10;

    /**
     * 通过线程模拟资源， 加锁和不枷锁的情况
     *
     * @param args
     */
    public static void main(String[] args) {
        threadSize = Integer.parseInt(args[0]);
        testWithLock = Integer.parseInt(args[1]) == 1;
        latch = new CountDownLatch(threadSize);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < threadSize; i++) {
            executorService.submit(new Operator());
        }

        try {
            latch.await();
            executorService.shutdown();
            boolean ok = shareData == threadSize;
            if (ok) {
                logger.info("^^^^^^^^^^^^^^^^^^^^ OK");
            } else {
                logger.info("#################### NOT OK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    static class Operator implements Runnable {
        private static Logger logger = LoggerFactory.getLogger(Operator.class);

        @Override
        public void run() {
            ZkMutexLock lock = null;
            try {
                if (testWithLock) {
                    ZkMutexLockFactory zkMutexLockFactory = new ZkMutexLockFactory();
                    lock = zkMutexLockFactory.lock(RESOURCE_NAME, TimeUnit.SECONDS.toNanos(3));//等待3s钟
                }

                //synchronized (ZkMutexLockUsage.class) {
                realOperation();
                //}
            } catch (Exception e) {
                logger.error("FFFFF ", e);
            } finally {
                ZkMutexLockUsage.latch.countDown();
                if (lock != null) {
                    try {
                        lock.unlock();
                    } catch (Exception exception) {
                        //解锁异常如何处理？
                        exception.printStackTrace();
                    }

                }
            }

        }

        private void realOperation() throws InterruptedException {
            logger.debug("###### REAL BUSINESS　######");
            Thread.sleep(10); // 增加10ms暂停以加速出现RACE CONDITION的概率
            ZkMutexLockUsage.shareData = ZkMutexLockUsage.shareData + 1;
            ZkMutexLockUsage.history.add(new Integer(ZkMutexLockUsage.shareData));
            logger.debug("--- Share Data --- " + ZkMutexLockUsage.shareData);
        }
    }

}
