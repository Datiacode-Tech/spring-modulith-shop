package com.datiacode.shop.order.internal;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import com.datiacode.shop.order.NewOrder;

record CreateOrderRequest(@NotBlank String customer, @NotEmpty List<@Valid Item> items) {

	record Item(@NotBlank String sku, @Positive int quantity) {
	}

	NewOrder toCommand() {
		return new NewOrder(customer, items.stream().map(item -> new NewOrder.Item(item.sku(), item.quantity())).toList());
	}
}
