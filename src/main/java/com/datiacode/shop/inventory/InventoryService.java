package com.datiacode.shop.inventory;

/**
 * Public API of the inventory module: how many units of each product are in stock.
 * <p>
 * Other modules may only depend on the types in this package; everything under {@code inventory.internal} is hidden
 * from them and checked by {@code ApplicationModules.verify()}.
 */
public interface InventoryService {

	/**
	 * @param sku product identifier
	 * @return units in stock, or {@code 0} if the product is not tracked
	 */
	int stockOf(String sku);
}
