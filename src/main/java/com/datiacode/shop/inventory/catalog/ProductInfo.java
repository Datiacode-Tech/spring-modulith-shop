package com.datiacode.shop.inventory.catalog;

import java.math.BigDecimal;

/**
 * Snapshot of a product as seen from outside the inventory module.
 */
public record ProductInfo(String sku, String name, BigDecimal unitPrice) {
}
