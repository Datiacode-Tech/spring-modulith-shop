package com.datiacode.shop.ai;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * What the ai module needs to write an order confirmation. Its own type, not {@code OrderCompleted}: the ai module
 * does not depend on order, the caller maps the event.
 */
public record OrderConfirmationRequest(Long orderId, String customer, List<Line> lines, BigDecimal total) {

	public OrderConfirmationRequest {
		Objects.requireNonNull(orderId, "orderId must not be null");
		Objects.requireNonNull(customer, "customer must not be null");
		Objects.requireNonNull(total, "total must not be null");
		lines = List.copyOf(lines);
	}

	public record Line(String productName, int quantity) {
	}
}
