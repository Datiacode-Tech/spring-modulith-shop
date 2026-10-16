package com.datiacode.shop.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Read model of a stored order, as exposed by {@link OrderService}.
 */
public record OrderDetails(Long id, String customer, String status, List<Line> lines, BigDecimal total,
		Instant createdAt) {

	public record Line(String sku, String productName, int quantity, BigDecimal unitPrice) {
	}
}
