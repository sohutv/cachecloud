package com.sohu.cache.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
@Slf4j
public class AsyncConfiguration {

    private int parallelism=256;

    @Bean
    public ForkJoinPool forkJoinPool() {
        log.info("availableProcessors:{}, parallelism:{}", Runtime.getRuntime().availableProcessors(), parallelism);
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
        return forkJoinPool;
    }
}