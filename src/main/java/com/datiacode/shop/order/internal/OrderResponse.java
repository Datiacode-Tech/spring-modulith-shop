package com.datiacode.shop.order.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.datiacode.shop.order.OrderDetails;

record OrderResponse(Long id, String customer, String status, List<Line> lines, BigDecimal total, Instant createdAt) {

	record Line(String sku, String productName, int quantity, BigDecimal unitPrice) {
	}

	static OrderResponse from(OrderDetails details) {
		var lines = details.lines().stream()
				.map(line -> new Line(line.sku(), line.productName(), line.quantity(), line.unitPrice()))
				.toList();
		return new OrderResponse(details.id(), details.customer(), details.status(), lines, details.total(),
				details.createdAt());
	}
}
