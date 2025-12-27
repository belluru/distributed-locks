package com.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;


public class DistributedLock {

    private static final String LOCK_KEY = "my-distributed-lock";
    private static final int LOCK_TTL_SECONDS = 1;
    private static final int NUM_CONSUMERS = 5;
    private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");

    public static void main(String[] args) {
        System.out.println("REDIS_HOST: " + REDIS_HOST);
        System.out.println("Application starting...");
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CONSUMERS);
        CountDownLatch latch = new CountDownLatch(NUM_CONSUMERS);

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            int consumerId = i;

            executor.submit(() -> {
                Jedis jedis = null;
                String lockValue = "consumer-" + consumerId;
                boolean lockAcquired = false;
                try {
                    jedis = new Jedis(REDIS_HOST, 6379);
                    // Retry acquiring lock with backoff
                    for (int attempt = 0; attempt < 15; attempt++) {
                        if (acquireLock(jedis, lockValue)) {
                            System.out.println("Consumer " + consumerId + " acquired the lock.");
                            lockAcquired = true;
                            // Simulate work that takes less than the lock TTL
                            Thread.sleep(500);
                            System.out.println("Consumer " + consumerId + " attempting to release the lock.");
                            releaseLock(jedis);
                            break;
                        } else if (attempt < 14) {
                            System.out.println("Consumer " + consumerId + " waiting to retry (attempt " + (attempt + 1) + ")...");
                            Thread.sleep(200);
                        }
                    }
                    if (!lockAcquired) {
                        System.out.println("Consumer " + consumerId + " failed to acquire the lock after retries.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Consumer " + consumerId + " interrupted.");
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for consumers.");
        }
        System.out.println("Application finished.");
    }

    private static boolean acquireLock(Jedis jedis, String lockValue) throws InterruptedException {
        SetParams setParams = new SetParams().nx().ex(LOCK_TTL_SECONDS);
        String result = jedis.set(LOCK_KEY, lockValue, setParams);
        return "OK".equals(result);
    }

    private static void releaseLock(Jedis jedis) {
        jedis.del(LOCK_KEY);
        System.out.println("Lock released successfully.");
    }
}
