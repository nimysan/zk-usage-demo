package com.vluee.demo.zkdemo.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模拟分布式环境下一次只允许一个进程处理某个数据
 */
public class ZkMutexLockUsage {

    private static CountDownLatch latch;

    private static int shareData = 0;
    private final static String RESOURCE_NAME = "DEMO_SHARE_DATA";
    private static List<Integer> history = new ArrayList<>();

    public static void main(String[] args) {
        int size = 5;
        latch = new CountDownLatch(size);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < size; i++) {
            executorService.submit(new Operator());
        }

        try {
            latch.await();
            executorService.shutdown();
//            for (int i = 0; i < size; i++) {
//                System.out.println("---- xx " + ZkMutexLockUsage.history.get(i));
//            }
            System.out.println(shareData);
        } catch (Exception e) {

        } finally {

        }

    }

    static class Operator implements Runnable {
        @Override
        public void run() {
            ZkMutexLock lock = null;
            try {
                ZkMutexLockFactory zkMutexLockFactory = new ZkMutexLockFactory();
                //
                lock = zkMutexLockFactory.lock(RESOURCE_NAME);
                //synchronized (ZkMutexLockUsage.class) {
                realOperation();
                //}
            } catch (Exception e) {
                e.printStackTrace();
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
            Thread.sleep(40);
            ZkMutexLockUsage.shareData = ZkMutexLockUsage.shareData + 1;
            ZkMutexLockUsage.history.add(new Integer(ZkMutexLockUsage.shareData));
            System.out.println("--- Share Data --- " + ZkMutexLockUsage.shareData);
        }
    }

}
