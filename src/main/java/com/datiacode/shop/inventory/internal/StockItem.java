package com.datiacode.shop.inventory.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
class StockItem {

	@Id
	private String sku;

	@Column(nullable = false)
	private int quantity;

	protected StockItem() {
		// for JPA
	}

	StockItem(String sku, int quantity) {
		this.sku = sku;
		this.quantity = quantity;
	}

	boolean hasAtLeast(int units) {
		return quantity >= units;
	}

	void decrease(int units) {
		if (!hasAtLeast(units)) {
			throw new IllegalStateException("Cannot take %d units of %s, only %d left".formatted(units, sku, quantity));
		}
		quantity -= units;
	}

	String getSku() {
		return sku;
	}

	int getQuantity() {
		return quantity;
	}
}
