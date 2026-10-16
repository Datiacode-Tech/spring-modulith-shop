package com.datiacode.shop.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datiacode.shop.inventory.InventoryService;
import com.datiacode.shop.inventory.catalog.ProductInfo;
import com.datiacode.shop.order.InsufficientStockException;
import com.datiacode.shop.order.NewOrder;

@ExtendWith(MockitoExtension.class)
class DefaultOrderServiceTest {

	static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
	static final ProductInfo KEYBOARD = new ProductInfo("KB-001", "Mechanical keyboard", new BigDecimal("89.90"));
	static final ProductInfo MOUSE = new ProductInfo("MS-001", "Wireless mouse", new BigDecimal("29.90"));

	@Mock
	OrderRepository orders;

	@Mock
	InventoryService inventory;

	DefaultOrderService service;

	@BeforeEach
	void setUp() {
		service = new DefaultOrderService(orders, inventory, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	@DisplayName("accepts the order and computes the total when every item is in stock")
	void acceptsOrderWhenInStock() {
		given(inventory.findProduct("KB-001")).willReturn(Optional.of(KEYBOARD));
		given(inventory.findProduct("MS-001")).willReturn(Optional.of(MOUSE));
		given(inventory.isAvailable(any(), anyInt())).willReturn(true);
		given(orders.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

		var details = service.create(new NewOrder("alice",
				List.of(new NewOrder.Item("KB-001", 1), new NewOrder.Item("MS-001", 2))));

		assertThat(details.customer()).isEqualTo("alice");
		assertThat(details.status()).isEqualTo("COMPLETED");
		assertThat(details.total()).isEqualByComparingTo("149.70");
		assertThat(details.createdAt()).isEqualTo(NOW);
		assertThat(details.lines()).hasSize(2);
	}

	@Test
	@DisplayName("rejects the order and stores nothing when an item is out of stock")
	void rejectsOrderWhenOutOfStock() {
		given(inventory.findProduct("KB-001")).willReturn(Optional.of(KEYBOARD));
		given(inventory.isAvailable("KB-001", 30)).willReturn(false);

		assertThatExceptionOfType(InsufficientStockException.class)
				.isThrownBy(() -> service.create(new NewOrder("alice", List.of(new NewOrder.Item("KB-001", 30)))))
				.satisfies(ex -> assertThat(ex.getSku()).isEqualTo("KB-001"));
		verify(orders, never()).save(any());
	}

	@Test
	@DisplayName("rejects the order when the SKU does not exist")
	void rejectsOrderWhenSkuUnknown() {
		given(inventory.findProduct("NOPE")).willReturn(Optional.empty());

		assertThatExceptionOfType(InsufficientStockException.class)
				.isThrownBy(() -> service.create(new NewOrder("alice", List.of(new NewOrder.Item("NOPE", 1)))));
		verify(orders, never()).save(any());
	}
}
