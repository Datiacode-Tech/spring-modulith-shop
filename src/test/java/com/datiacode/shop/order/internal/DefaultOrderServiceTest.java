package com.datiacode.shop.order.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.datiacode.shop.catalog.CatalogService;
import com.datiacode.shop.catalog.model.ProductInfo;
import com.datiacode.shop.order.NewOrder;
import com.datiacode.shop.order.OrderCompleted;
import com.datiacode.shop.order.UnknownProductException;

@ExtendWith(MockitoExtension.class)
class DefaultOrderServiceTest {

	static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
	static final ProductInfo KEYBOARD = new ProductInfo("KB-001", "Mechanical keyboard", new BigDecimal("89.90"));
	static final ProductInfo MOUSE = new ProductInfo("MS-001", "Wireless mouse", new BigDecimal("29.90"));

	@Mock
	OrderRepository orders;

	@Mock
	CatalogService catalog;

	@Mock
	ApplicationEventPublisher events;

	DefaultOrderService service;

	@BeforeEach
	void setUp() {
		service = new DefaultOrderService(orders, catalog, events, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	@DisplayName("stores the order with catalog prices and computes the total")
	void storesOrderWithCatalogPrices() {
		given(catalog.findProduct("KB-001")).willReturn(Optional.of(KEYBOARD));
		given(catalog.findProduct("MS-001")).willReturn(Optional.of(MOUSE));
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
	@DisplayName("publishes exactly one OrderCompleted with the order data when the order is accepted")
	void publishesOrderCompleted() {
		given(catalog.findProduct("KB-001")).willReturn(Optional.of(KEYBOARD));
		given(orders.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

		service.create(new NewOrder("alice", List.of(new NewOrder.Item("KB-001", 2))));

		var captor = ArgumentCaptor.forClass(OrderCompleted.class);
		verify(events, times(1)).publishEvent(captor.capture());
		var event = captor.getValue();
		assertThat(event.customer()).isEqualTo("alice");
		assertThat(event.total()).isEqualByComparingTo("179.80");
		assertThat(event.completedAt()).isEqualTo(NOW);
		assertThat(event.lines()).containsExactly(new OrderCompleted.Line("KB-001", "Mechanical keyboard", 2));
	}

	@Test
	@DisplayName("rejects the order, stores nothing and publishes nothing when the SKU is not in the catalog")
	void rejectsOrderWhenSkuUnknown() {
		given(catalog.findProduct("NOPE")).willReturn(Optional.empty());

		assertThatExceptionOfType(UnknownProductException.class)
				.isThrownBy(() -> service.create(new NewOrder("alice", List.of(new NewOrder.Item("NOPE", 1)))))
				.satisfies(ex -> assertThat(ex.getSku()).isEqualTo("NOPE"));
		verify(orders, never()).save(any());
		verify(events, never()).publishEvent(any());
	}
}
