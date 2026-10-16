/**
 * Read-only product catalog data that the inventory module shares with other modules.
 * <p>
 * Exposed as the named interface {@code inventory::catalog}: a sub-package that would otherwise
 * be internal to the module.
 */
@org.springframework.modulith.NamedInterface("catalog")
package com.datiacode.shop.inventory.catalog;
