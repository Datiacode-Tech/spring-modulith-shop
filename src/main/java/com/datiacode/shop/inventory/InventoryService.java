package com.datiacode.shop.inventory;

import java.util.Optional;

import com.datiacode.shop.inventory.catalog.ProductInfo;

/**
 * Public API of the inventory module.
 * <p>
 * Other modules may only depend on the types in this package; everything under
 * {@code inventory.internal} is hidden from them and checked by {@code ApplicationModules.verify()}.
 */
public interface InventoryService {

	/**
	 * Tells whether the requested quantity of a product can be served right now.
	 *
	 * @param sku product identifier
	 * @param quantity requested units, must be positive
	 * @return {@code true} if the product exists and has at least {@code quantity} units in stock
	 */
	boolean isAvailable(String sku, int quantity);

	/**
	 * Looks up the catalog data of a product.
	 *
	 * @param sku product identifier
	 * @return the product, or empty if the SKU does not exist
	 */
	Optional<ProductInfo> findProduct(String sku);
}
