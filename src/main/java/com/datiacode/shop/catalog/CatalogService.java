package com.datiacode.shop.catalog;

import java.util.Optional;

import com.datiacode.shop.catalog.model.ProductInfo;

/**
 * Public API of the catalog module: what is sold and at which price.
 * <p>
 * Other modules may only depend on this package and the {@code catalog::model} named interface; everything under
 * {@code catalog.internal} is hidden from them and checked by {@code ApplicationModules.verify()}.
 */
public interface CatalogService {

	/**
	 * Looks up a product.
	 *
	 * @param sku product identifier
	 * @return the product, or empty if the SKU does not exist
	 */
	Optional<ProductInfo> findProduct(String sku);
}
