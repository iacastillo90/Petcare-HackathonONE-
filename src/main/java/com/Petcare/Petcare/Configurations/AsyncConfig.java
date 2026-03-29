package com.Petcare.Petcare.Configurations;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * Configuration for async processing with Spring Security support.
 * 
 * This configuration:
 * - Enables @Async annotation support
 * - Configures ThreadPoolTaskExecutor for async operations
 * - Wraps executor with DelegatingSecurityContextAsyncTaskExecutor
 *   to propagate SecurityContext to async threads
 * 
 * The DelegatingSecurityContextAsyncTaskExecutor ensures that:
 * - SecurityContext is available in async threads
 * - @AuthenticationPrincipal works correctly
 * - SecurityContextHolder.getContext() returns the correct context
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Create async task executor with security context propagation.
     * 
     * This executor:
     * - Uses a thread pool for async operations
     * - Propagates SecurityContext to async threads
     * - Works with @Async and CompletableFuture
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - threads kept alive even when idle
        executor.setCorePoolSize(10);
        
        // Maximum pool size
        executor.setMaxPoolSize(50);
        
        // Queue capacity - tasks queued when all threads are busy
        executor.setQueueCapacity(200);
        
        // Thread name prefix for debugging
        executor.setThreadNamePrefix("async-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Timeout for shutdown
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        // Wrap with DelegatingSecurityContextAsyncTaskExecutor
        // This is CRITICAL for Spring Security integration
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
