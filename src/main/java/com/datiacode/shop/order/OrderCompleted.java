package com.datiacode.shop.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.modulith.events.Externalized;

/**
 * Published when an order has been accepted and stored. The order module does not know who listens.
 * <p>
 * Also externalized to the Kafka topic {@code shop.orders.completed}, keyed by order id, for consumers outside the
 * application (e.g. an analytics team).
 */
@Externalized("shop.orders.completed::#{#this.orderId().toString()}")
public record OrderCompleted(Long orderId, String customer, List<Line> lines, BigDecimal total, Instant completedAt) {

	public OrderCompleted {
		lines = List.copyOf(lines);
	}

	public record Line(String sku, String productName, int quantity) {
	}
}
