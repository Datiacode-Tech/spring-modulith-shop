package com.datiacode.shop.inventory.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryServiceTest {

	@Mock
	StockItemRepository stock;

	@InjectMocks
	DefaultInventoryService inventory;

	@Test
	@DisplayName("returns the units in stock of a tracked product")
	void returnsStock() {
		given(stock.findById("KB-001")).willReturn(Optional.of(new StockItem("KB-001", 25)));

		assertThat(inventory.stockOf("KB-001")).isEqualTo(25);
	}

	@Test
	@DisplayName("returns zero for a product that is not tracked")
	void returnsZeroForUnknownSku() {
		given(stock.findById("NOPE")).willReturn(Optional.empty());

		assertThat(inventory.stockOf("NOPE")).isZero();
	}
}
