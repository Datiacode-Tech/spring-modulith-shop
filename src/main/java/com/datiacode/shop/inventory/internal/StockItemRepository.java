package com.datiacode.shop.inventory.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface StockItemRepository extends JpaRepository<StockItem, String> {
}
