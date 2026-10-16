package com.datiacode.shop.order;

import java.util.List;
import java.util.Objects;

/**
 * Command to place an order.
 */
public record NewOrder(String customer, List<Item> items) {

	public NewOrder {
		Objects.requireNonNull(customer, "customer must not be null");
		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("An order needs at least one item");
		}
		items = List.copyOf(items);
	}

	public record Item(String sku, int quantity) {
	}
}
