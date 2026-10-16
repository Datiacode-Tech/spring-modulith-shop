package com.datiacode.shop.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.datiacode.shop.TestcontainersConfiguration;
import com.datiacode.shop.order.OrderCompleted;

/**
 * Boots only the inventory module and stimulates it with an event, as if the order module had published it.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class InventoryModuleTests {

	private final InventoryService inventory;

	InventoryModuleTests(InventoryService inventory) {
		this.inventory = inventory;
	}

	@Test
	@DisplayName("OrderCompleted takes the ordered units out of stock")
	void orderCompletedDecreasesStock(Scenario scenario) {
		int before = inventory.stockOf("MS-001");
		var event = new OrderCompleted(42L, "alice", List.of(new OrderCompleted.Line("MS-001", "Wireless mouse", 3)),
				new BigDecimal("89.70"), Instant.now());

		scenario.publish(event)
				.andWaitForStateChange(() -> inventory.stockOf("MS-001"), stock -> stock != before)
				.andVerify(stock -> assertThat(stock).isEqualTo(before - 3));
	}
}
