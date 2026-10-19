package com.datiacode.shop.order.internal;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import com.datiacode.shop.order.NewOrder;

/**
 * The customer is not part of the request: it is the authenticated user.
 */
record CreateOrderRequest(@NotEmpty List<@Valid Item> items) {

	record Item(@NotBlank String sku, @Positive int quantity) {
	}

	NewOrder toCommand(String customer) {
		return new NewOrder(customer,
				items.stream().map(item -> new NewOrder.Item(item.sku(), item.quantity())).toList());
	}
}
