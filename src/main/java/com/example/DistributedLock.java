package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DistributedLock {

    private static final String LOCK_KEY = "my-distributed-lock";
    private static final String LOCK_VALUE = "locked";
    private static final int LOCK_TTL_SECONDS = 1;
    private static final int NUM_CONSUMERS = 1000;
    private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CONSUMERS);

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            int consumerId = i;
            executor.submit(() -> {
                try (Jedis jedis = new Jedis(REDIS_HOST)) {
                    if (acquireLock(jedis)) {
                        try {
                            System.out.println("Consumer " + consumerId + " acquired the lock.");
                            // Simulate some work
                            Thread.sleep(500);
                        } finally {
                            releaseLock(jedis);
                            System.out.println("Consumer " + consumerId + " released the lock.");
                        }
                    } else {
                        System.out.println("Consumer " + consumerId + " failed to acquire the lock.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private static boolean acquireLock(Jedis jedis) {
        SetParams setParams = new SetParams().nx().ex(LOCK_TTL_SECONDS);
        String result = jedis.set(LOCK_KEY, LOCK_VALUE, setParams);
        return "OK".equals(result);
    }

    private static void releaseLock(Jedis jedis) {
        if (LOCK_VALUE.equals(jedis.get(LOCK_KEY))) {
            jedis.del(LOCK_KEY);
        }
    }
}
