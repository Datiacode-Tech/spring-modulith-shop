package com.datiacode.shop;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Fails the build if any module reaches into another module's internals or if module dependencies form a cycle.
 */
class ModularityTest {

	static final ApplicationModules MODULES = ApplicationModules.of(ShopApplication.class);

	@Test
	void verifiesModularStructure() {
		MODULES.verify();
	}

	@Test
	void exposesCatalogModelAsNamedInterface() {
		var catalog = MODULES.getModuleByName("catalog").orElseThrow();

		assertThat(catalog.getNamedInterfaces().getByName("model")).isPresent();
	}

	@Test
	void orderDoesNotDependOnInventory() {
		var order = MODULES.getModuleByName("order").orElseThrow();

		// "Pedidos ya no conoce a inventario": inventory listens to order's events, never the other way round
		assertThat(order.getAllDependencies(MODULES).containsModuleNamed("inventory")).isFalse();
	}

	@Test
	void onlyAiInternalUsesSpringAi() {
		var productionClasses = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages("com.datiacode.shop");

		// verify() only checks dependencies between application modules: Spring AI is a library, not a module, so a
		// ChatClient injected anywhere would pass it. This rule keeps the provider behind the ai module's TextWriter.
		noClasses().that().resideOutsideOfPackage("com.datiacode.shop.ai.internal..")
				.should().dependOnClassesThat().resideInAPackage("org.springframework.ai..")
				.because("the AI provider is an internal detail of the ai module; use ai.TextWriter instead")
				.check(productionClasses);
	}
}
