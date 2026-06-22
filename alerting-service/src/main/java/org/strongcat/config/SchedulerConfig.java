package org.strongcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.strongcat.constant.SchedulerConstant.*;

@Configuration
public class SchedulerConfig {

    @Bean
    public ConcurrentTaskScheduler taskScheduler() {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(
                SCHEDULER_CORE_POOL_SIZE,
                Thread.ofVirtual().name(VIRTUAL_THREAD_NAME_PREFIX, THREAD_INITIAL_COUNTER).factory()
        );
        return new ConcurrentTaskScheduler(executor);
    }
}