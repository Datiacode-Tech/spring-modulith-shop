package com.datiacode.shop.inventory.internal;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String sku;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private int stock;

	protected Product() {
		// for JPA
	}

	Product(String sku, String name, BigDecimal price, int stock) {
		this.sku = sku;
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	boolean hasStock(int quantity) {
		return stock >= quantity;
	}

	String getSku() {
		return sku;
	}

	String getName() {
		return name;
	}

	BigDecimal getPrice() {
		return price;
	}

	int getStock() {
		return stock;
	}
}
