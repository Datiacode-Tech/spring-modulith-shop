package com.datiacode.shop.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Published when an order has been accepted and stored. The order module does not know who listens.
 */
public record OrderCompleted(Long orderId, String customer, List<Line> lines, BigDecimal total, Instant completedAt) {

	public OrderCompleted {
		lines = List.copyOf(lines);
	}

	public record Line(String sku, String productName, int quantity) {
	}
}
