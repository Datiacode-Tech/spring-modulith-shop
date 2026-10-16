package com.datiacode.shop.inventory.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datiacode.shop.inventory.catalog.ProductInfo;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryServiceTest {

	@Mock
	ProductRepository products;

	@InjectMocks
	DefaultInventoryService inventory;

	@Test
	@DisplayName("is available when stock covers the requested quantity")
	void availableWhenEnoughStock() {
		given(products.findBySku("KB-001")).willReturn(Optional.of(keyboardWithStock(5)));

		assertThat(inventory.isAvailable("KB-001", 5)).isTrue();
	}

	@Test
	@DisplayName("is not available when stock is below the requested quantity")
	void notAvailableWhenNotEnoughStock() {
		given(products.findBySku("KB-001")).willReturn(Optional.of(keyboardWithStock(2)));

		assertThat(inventory.isAvailable("KB-001", 3)).isFalse();
	}

	@Test
	@DisplayName("is not available when the SKU does not exist")
	void notAvailableWhenSkuUnknown() {
		given(products.findBySku("NOPE")).willReturn(Optional.empty());

		assertThat(inventory.isAvailable("NOPE", 1)).isFalse();
	}

	@Test
	@DisplayName("rejects non-positive quantities")
	void rejectsNonPositiveQuantity() {
		assertThatIllegalArgumentException().isThrownBy(() -> inventory.isAvailable("KB-001", 0));
	}

	@Test
	@DisplayName("exposes catalog data of an existing product")
	void findsProductInfo() {
		given(products.findBySku("KB-001")).willReturn(Optional.of(keyboardWithStock(5)));

		assertThat(inventory.findProduct("KB-001"))
				.contains(new ProductInfo("KB-001", "Mechanical keyboard", new BigDecimal("89.90")));
	}

	@Test
	@DisplayName("returns empty catalog data for an unknown SKU")
	void findsNothingForUnknownSku() {
		given(products.findBySku("NOPE")).willReturn(Optional.empty());

		assertThat(inventory.findProduct("NOPE")).isEmpty();
	}

	private static Product keyboardWithStock(int stock) {
		return new Product("KB-001", "Mechanical keyboard", new BigDecimal("89.90"), stock);
	}
}
