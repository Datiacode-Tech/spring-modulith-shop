package com.datiacode.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.datiacode.shop.TestcontainersConfiguration;
import com.datiacode.shop.catalog.CatalogService;
import com.datiacode.shop.catalog.model.ProductInfo;

/**
 * Boots only the order module: the catalog is a mock and no listener of other modules is running. The test checks
 * the module's output, the {@link OrderCompleted} event, not who reacts to it.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class OrderModuleTests {

	@MockitoBean
	CatalogService catalog;

	private final OrderService orders;

	OrderModuleTests(OrderService orders) {
		this.orders = orders;
	}

	@Test
	@DisplayName("placing an order publishes OrderCompleted with the stored id, lines and total")
	void placingAnOrderPublishesOrderCompleted(Scenario scenario) {
		given(catalog.findProduct("KB-001"))
				.willReturn(Optional.of(new ProductInfo("KB-001", "Mechanical keyboard", new BigDecimal("89.90"))));

		scenario.stimulate(() -> orders.create(new NewOrder("alice", List.of(new NewOrder.Item("KB-001", 2)))))
				.andWaitForEventOfType(OrderCompleted.class)
				.toArriveAndVerify((event, order) -> {
					assertThat(event.orderId()).isEqualTo(order.id());
					assertThat(event.customer()).isEqualTo("alice");
					assertThat(event.lines())
							.containsExactly(new OrderCompleted.Line("KB-001", "Mechanical keyboard", 2));
					assertThat(event.total()).isEqualByComparingTo("179.80");
				});
	}
}
