package com.datiacode.shop.order.internal;

import org.springframework.stereotype.Component;

import com.datiacode.shop.catalog.internal.ProductRepository;

/**
 * DEMO ONLY: reaches into catalog's internals instead of using CatalogService.
 */
@Component
class StockShortcut {

	private final ProductRepository products;

	StockShortcut(ProductRepository products) {
		this.products = products;
	}

	long countProducts() {
		return products.count();
	}
}
