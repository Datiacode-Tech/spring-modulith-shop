package com.datiacode.shop.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async}, which {@code @ApplicationModuleListener} relies on. Listeners run on Spring Boot's
 * {@code applicationTaskExecutor}, tuned through {@code spring.task.execution.*}.
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
class AsyncConfig {
}
