package com.datiacode.shop.catalog.model;

import java.math.BigDecimal;

/**
 * Snapshot of a product as seen from outside the catalog module.
 */
public record ProductInfo(String sku, String name, BigDecimal unitPrice) {
}
