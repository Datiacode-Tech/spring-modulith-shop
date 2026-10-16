package com.datiacode.shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.Options;

/**
 * Generates living documentation (PlantUML component diagrams, module canvases and a plain-text module summary
 * including named interfaces) into {@code docs/modules}.
 * Excluded from the default build; run with {@code ./mvnw test -Pdocs}.
 */
@Tag("documentation")
class DocumentationTest {

	@Test
	void writesModuleDocumentation() throws IOException {
		var modules = ApplicationModules.of(ShopApplication.class);

		new Documenter(modules, Options.defaults().withOutputFolder("docs/modules"))
				.writeDocumentation()
				.writeModulesAsPlantUml();

		// Canvases do not list named interfaces; the module description does
		var summary = modules.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator()));
		Files.writeString(Path.of("docs/modules/modules.txt"), summary);
	}
}
