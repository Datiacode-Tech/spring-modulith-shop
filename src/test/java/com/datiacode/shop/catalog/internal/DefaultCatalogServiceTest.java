package com.datiacode.shop.catalog.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datiacode.shop.catalog.model.ProductInfo;

@ExtendWith(MockitoExtension.class)
class DefaultCatalogServiceTest {

	@Mock
	ProductRepository products;

	@InjectMocks
	DefaultCatalogService catalog;

	@Test
	@DisplayName("exposes name and price of an existing product")
	void findsProductInfo() {
		given(products.findBySku("KB-001"))
				.willReturn(Optional.of(new Product("KB-001", "Mechanical keyboard", new BigDecimal("89.90"))));

		assertThat(catalog.findProduct("KB-001"))
				.contains(new ProductInfo("KB-001", "Mechanical keyboard", new BigDecimal("89.90")));
	}

	@Test
	@DisplayName("returns empty for an unknown SKU")
	void findsNothingForUnknownSku() {
		given(products.findBySku("NOPE")).willReturn(Optional.empty());

		assertThat(catalog.findProduct("NOPE")).isEmpty();
	}
}
