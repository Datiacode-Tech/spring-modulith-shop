package com.datiacode.shop.inventory.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datiacode.shop.inventory.InventoryService;

@Service
@Transactional(readOnly = true)
class DefaultInventoryService implements InventoryService {

	private final StockItemRepository stock;

	DefaultInventoryService(StockItemRepository stock) {
		this.stock = stock;
	}

	@Override
	public int stockOf(String sku) {
		return stock.findById(sku).map(StockItem::getQuantity).orElse(0);
	}
}
