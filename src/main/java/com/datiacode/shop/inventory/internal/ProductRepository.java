package com.datiacode.shop.inventory.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findBySku(String sku);
}
