package com.friendslikethese.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ConcurrencyConfig {
    @Bean(name = "friendsLikeTheseExecutor")
    ThreadPoolTaskExecutor friendsLikeTheseExecutor(
            @Value("${app.executor.core-pool-size:2}") int corePoolSize,
            @Value("${app.executor.max-pool-size:4}") int maxPoolSize,
            @Value("${app.executor.queue-capacity:32}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("friends-like-these-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
