package com.datiacode.shop.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Uses the same decorator that Spring Modulith installs on the task executor, to check what reaches the listener
 * thread and that nothing is left behind on it.
 */
class ContextPropagationConfigTest {

	final ContextPropagatingTaskDecorator decorator = new ContextPropagatingTaskDecorator();

	// A single thread, so the second task runs on the very thread that ran the first one
	final ExecutorService executor = Executors.newSingleThreadExecutor();

	@BeforeAll
	static void registerAccessors() {
		ContextPropagationConfig.registerTraceIdAccessor();
	}

	@AfterEach
	void tearDown() {
		executor.shutdownNow();
		SecurityContextHolder.clearContext();
		MDC.clear();
	}

	@Test
	@DisplayName("the task sees the caller's user and trace id on another thread")
	void propagatesSecurityContextAndTraceId() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alice", "n/a"));
		MDC.put("traceId", "abc123");
		var seenUser = new AtomicReference<String>();
		var seenTraceId = new AtomicReference<String>();

		executor.submit(decorator.decorate(() -> {
			seenUser.set(SecurityContextHolder.getContext().getAuthentication().getName());
			seenTraceId.set(MDC.get("traceId"));
		})).get(5, TimeUnit.SECONDS);

		assertThat(seenUser).hasValue("alice");
		assertThat(seenTraceId).hasValue("abc123");
	}

	@Test
	@DisplayName("the pooled thread is clean after the task, so the next task does not inherit the previous user")
	void leavesNothingBehind() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alice", "n/a"));
		MDC.put("traceId", "abc123");
		executor.submit(decorator.decorate(() -> { })).get(5, TimeUnit.SECONDS);

		var leakedAuthentication = new AtomicReference<Object>("not checked");
		var leakedTraceId = new AtomicReference<String>("not checked");
		executor.submit(() -> {
			leakedAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
			leakedTraceId.set(MDC.get("traceId"));
		}).get(5, TimeUnit.SECONDS);

		assertThat(leakedAuthentication).hasValue(null);
		assertThat(leakedTraceId).hasValue(null);
	}
}
