package com.datiacode.shop.catalog.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datiacode.shop.catalog.CatalogService;
import com.datiacode.shop.catalog.model.ProductInfo;

@Service
@Transactional(readOnly = true)
class DefaultCatalogService implements CatalogService {

	private final ProductRepository products;

	DefaultCatalogService(ProductRepository products) {
		this.products = products;
	}

	@Override
	public Optional<ProductInfo> findProduct(String sku) {
		return products.findBySku(sku)
				.map(product -> new ProductInfo(product.getSku(), product.getName(), product.getPrice()));
	}
}
