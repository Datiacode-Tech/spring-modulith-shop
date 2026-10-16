package com.datiacode.shop.order;

/**
 * Thrown when an order cannot be placed because a product is unknown or out of stock.
 */
public class InsufficientStockException extends RuntimeException {

	private final String sku;

	public InsufficientStockException(String sku, int quantity) {
		super("Product %s is not available in quantity %d".formatted(sku, quantity));
		this.sku = sku;
	}

	public String getSku() {
		return sku;
	}
}
