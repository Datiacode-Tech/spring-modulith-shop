package com.datiacode.shop.order;

/**
 * Thrown when an order references a product that is not in the catalog.
 */
public class UnknownProductException extends RuntimeException {

	private final String sku;

	public UnknownProductException(String sku) {
		super("Product %s does not exist".formatted(sku));
		this.sku = sku;
	}

	public String getSku() {
		return sku;
	}
}
