package com.datiacode.shop.inventory.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datiacode.shop.inventory.InventoryService;
import com.datiacode.shop.inventory.catalog.ProductInfo;

@Service
@Transactional(readOnly = true)
class DefaultInventoryService implements InventoryService {

	private final ProductRepository products;

	DefaultInventoryService(ProductRepository products) {
		this.products = products;
	}

	@Override
	public boolean isAvailable(String sku, int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be positive, got " + quantity);
		}
		return products.findBySku(sku)
				.map(product -> product.hasStock(quantity))
				.orElse(false);
	}

	@Override
	public Optional<ProductInfo> findProduct(String sku) {
		return products.findBySku(sku)
				.map(product -> new ProductInfo(product.getSku(), product.getName(), product.getPrice()));
	}
}
