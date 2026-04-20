package com.slotify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final RedissonClient redissonClient;

    private static final String SEAT_LOCK_PREFIX = "lock:seat:event:";
    private static final long WAIT_TIME = 5L;
    private static final long LEASE_TIME = 10L;

    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        RLock lock = redissonClient.getLock(SEAT_LOCK_PREFIX + lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Could not acquire lock for: " + lockKey
                        + " — system is under heavy load, please retry");
            }
            log.info("Lock acquired for key: {}", lockKey);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted for: " + lockKey);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for key: {}", lockKey);
            }
        }
    }
}