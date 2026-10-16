package com.datiacode.shop.order.internal;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class OrderLine {

	@Column(nullable = false)
	private String sku;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false)
	private BigDecimal unitPrice;

	protected OrderLine() {
		// for JPA
	}

	OrderLine(String sku, String productName, int quantity, BigDecimal unitPrice) {
		this.sku = sku;
		this.productName = productName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	BigDecimal subtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	String getSku() {
		return sku;
	}

	String getProductName() {
		return productName;
	}

	int getQuantity() {
		return quantity;
	}

	BigDecimal getUnitPrice() {
		return unitPrice;
	}
}
