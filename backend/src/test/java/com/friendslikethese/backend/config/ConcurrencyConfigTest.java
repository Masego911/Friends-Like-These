package com.friendslikethese.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrencyConfigTest {
    @Test
    void applicationExecutorIsBoundedAndNamed() {
        ThreadPoolTaskExecutor executor = new ConcurrencyConfig().friendsLikeTheseExecutor(2, 4, 32);
        executor.initialize();
        try {
            assertEquals(2, executor.getCorePoolSize());
            assertEquals(4, executor.getMaxPoolSize());
            assertEquals(32, executor.getQueueCapacity());
            assertEquals("friends-like-these-", executor.getThreadNamePrefix());
        } finally {
            executor.shutdown();
        }
    }
}
