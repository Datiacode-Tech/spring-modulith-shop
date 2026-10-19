package com.datiacode.shop.shared.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import io.micrometer.context.ContextRegistry;

/**
 * Makes the MDC trace id travel with asynchronous tasks, including every {@code @ApplicationModuleListener}.
 * <p>
 * There is no custom {@code TaskDecorator} on purpose: {@code spring-modulith-observability} already installs Spring's
 * {@code ContextPropagatingTaskDecorator} on the task executor (and would silently replace one of ours). That decorator
 * propagates whatever is registered in Micrometer's {@link ContextRegistry}: Spring Security registers the
 * {@code SecurityContext} itself, so only the MDC entry needs registering here. The decorator also restores the
 * worker thread's previous state after each task, so pooled threads do not leak another request's data.
 */
@Configuration(proxyBeanMethods = false)
class ContextPropagationConfig {

	static final String TRACE_ID = "traceId";

	ContextPropagationConfig() {
		registerTraceIdAccessor();
	}

	static void registerTraceIdAccessor() {
		// Re-registering the same key replaces the previous accessor, so this is safe across test contexts
		ContextRegistry.getInstance().registerThreadLocalAccessor(TRACE_ID,
				() -> MDC.get(TRACE_ID),
				value -> MDC.put(TRACE_ID, value),
				() -> MDC.remove(TRACE_ID));
	}
}
