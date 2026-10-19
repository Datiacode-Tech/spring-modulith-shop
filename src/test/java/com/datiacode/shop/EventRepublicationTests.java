package com.datiacode.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.datiacode.shop.order.NewOrder;
import com.datiacode.shop.order.OrderCompleted;
import com.datiacode.shop.order.OrderService;

/**
 * Proves the Event Publication Registry survives a restart: a listener that fails leaves its publication incomplete in
 * {@code event_publication}, and the next application start resubmits it.
 * <p>
 * Two real application contexts are started one after the other against the same PostgreSQL container.
 */
class EventRepublicationTests {

	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@BeforeAll
	static void startDatabase() {
		POSTGRES.start();
	}

	@AfterAll
	static void stopDatabase() {
		POSTGRES.stop();
	}

	@Test
	@DisplayName("a publication whose listener failed is completed after restarting the application")
	void republishesIncompletePublicationOnRestart() {
		FlakyListener.failing.set(true);

		try (var app = startApplication()) {
			app.getBean(OrderService.class).create(new NewOrder("alice", List.of(new NewOrder.Item("MS-001", 1))));

			await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
				assertThat(FlakyListener.attempts).hasValueGreaterThanOrEqualTo(1);
				assertThat(incompleteFlakyPublications(app)).isEqualTo(1);
			});
		}

		// The "bug" is fixed and the application restarts
		FlakyListener.failing.set(false);

		try (var app = startApplication()) {
			await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
				assertThat(FlakyListener.handled).hasValue(1);
				assertThat(incompleteFlakyPublications(app)).isZero();
			});
		}
	}

	private static ConfigurableApplicationContext startApplication() {
		return new SpringApplicationBuilder(ShopApplication.class, FlakyListener.class)
				.properties(
						"server.port=0",
						"spring.datasource.url=" + POSTGRES.getJdbcUrl(),
						"spring.datasource.username=" + POSTGRES.getUsername(),
						"spring.datasource.password=" + POSTGRES.getPassword(),
						"spring.modulith.events.republish-outstanding-events-on-restart=true")
				.run();
	}

	private static Integer incompleteFlakyPublications(ConfigurableApplicationContext app) {
		return app.getBean(JdbcTemplate.class).queryForObject("""
				SELECT count(*) FROM event_publication
				WHERE completion_date IS NULL AND listener_id LIKE '%FlakyListener%'
				""", Integer.class);
	}

	/**
	 * Listener that fails while {@link #failing} is set, standing in for a bug or a crash in a real listener.
	 */
	static class FlakyListener {

		static final AtomicBoolean failing = new AtomicBoolean();
		static final AtomicInteger attempts = new AtomicInteger();
		static final AtomicInteger handled = new AtomicInteger();

		@ApplicationModuleListener
		void on(OrderCompleted event) {
			attempts.incrementAndGet();
			if (failing.get()) {
				throw new IllegalStateException("Simulated failure handling order " + event.orderId());
			}
			handled.incrementAndGet();
		}
	}
}
