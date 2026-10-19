package com.datiacode.shop;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.redpanda.RedpandaContainer;

import com.datiacode.shop.order.NewOrder;
import com.datiacode.shop.order.OrderService;

/**
 * {@code OrderCompleted} leaves the application through Kafka (Redpanda) without the order module knowing about it.
 */
@SpringBootTest(properties = "spring.modulith.events.externalization.enabled=true")
@Import({ TestcontainersConfiguration.class, ExternalizationTests.Redpanda.class })
class ExternalizationTests {

	static final String TOPIC = "shop.orders.completed";

	@TestConfiguration(proxyBeanMethods = false)
	static class Redpanda {

		@Bean
		@ServiceConnection
		RedpandaContainer redpanda() {
			return new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v24.2.4");
		}
	}

	@Autowired
	OrderService orders;

	@Autowired
	RedpandaContainer redpanda;

	@Test
	@DisplayName("a placed order is published to shop.orders.completed keyed by order id")
	void externalizesOrderCompleted() {
		var order = orders.create(new NewOrder("alice", List.of(new NewOrder.Item("KB-001", 1))));

		try (var consumer = new KafkaConsumer<String, String>(Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, redpanda.getBootstrapServers(),
				ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
			consumer.subscribe(List.of(TOPIC));

			var received = new ArrayList<ConsumerRecord<String, String>>();
			long deadline = System.currentTimeMillis() + 20_000;
			while (received.isEmpty() && System.currentTimeMillis() < deadline) {
				consumer.poll(Duration.ofMillis(500)).forEach(received::add);
			}

			assertThat(received).singleElement().satisfies(record -> {
				assertThat(record.key()).isEqualTo(order.id().toString());
				assertThat(record.value()).contains("\"customer\":\"alice\"").contains("\"sku\":\"KB-001\"");
			});
		}
	}
}
