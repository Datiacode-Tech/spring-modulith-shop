package com.datiacode.shop.inventory.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datiacode.shop.order.OrderCompleted;

@ExtendWith(MockitoExtension.class)
class OrderCompletedListenerTest {

	@Mock
	StockItemRepository stock;

	@InjectMocks
	OrderCompletedListener listener;

	@Test
	@DisplayName("takes the ordered units out of stock")
	void decreasesStock() {
		var keyboards = new StockItem("KB-001", 25);
		given(stock.findById("KB-001")).willReturn(Optional.of(keyboards));

		listener.on(orderOf("KB-001", 2));

		assertThat(keyboards.getQuantity()).isEqualTo(23);
	}

	@Test
	@DisplayName("leaves stock untouched and does not fail when there is a shortage")
	void toleratesShortage() {
		var hubs = new StockItem("HD-001", 0);
		given(stock.findById("HD-001")).willReturn(Optional.of(hubs));

		listener.on(orderOf("HD-001", 1));

		assertThat(hubs.getQuantity()).isZero();
	}

	@Test
	@DisplayName("does not fail when the product is not tracked")
	void toleratesUntrackedProduct() {
		given(stock.findById("NOPE")).willReturn(Optional.empty());

		listener.on(orderOf("NOPE", 1));
	}

	private static OrderCompleted orderOf(String sku, int quantity) {
		return new OrderCompleted(1L, "alice", List.of(new OrderCompleted.Line(sku, "Some product", quantity)),
				BigDecimal.TEN, Instant.now());
	}
}
