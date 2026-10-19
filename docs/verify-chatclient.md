# ¿Detecta `verify()` un `ChatClient` usado fuera del módulo `ai`?

Experimento de F4-06 (2026-10-19). Comprueba la afirmación del Post 9: *"verify() vigila nuestras clases, pero no impide que otro módulo use ChatClient directamente, porque es una librería"*.

## El atajo

Una clase en `notification` que usa Spring AI directamente, saltándose `ai.TextWriter`:

```java
package com.datiacode.shop.notification.internal;

@Component
class ShortcutWriter {

	private final ChatClient chatClient;

	ShortcutWriter(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}

	String write(String prompt) {
		return chatClient.prompt(prompt).call().content();
	}
}
```

## 1. Solo con `ApplicationModules.verify()`

```text
$ ./mvnw test -Dtest=ModularityTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Pasa.** `verify()` comprueba las dependencias entre módulos de la aplicación (accesos a `internal`, ciclos, dependencias permitidas). `org.springframework.ai` no es un módulo de la aplicación, sino una librería, así que para `verify()` usarla desde `notification` es tan legítimo como usar `java.util.List`.

## 2. Con una regla de ArchUnit

`ModularityTest.onlyAiInternalUsesSpringAi()`:

```java
noClasses().that().resideOutsideOfPackage("com.datiacode.shop.ai.internal..")
		.should().dependOnClassesThat().resideInAPackage("org.springframework.ai..")
		.because("the AI provider is an internal detail of the ai module; use ai.TextWriter instead")
		.check(productionClasses);
```

Con el atajo todavía en `notification`:

```text
$ ./mvnw test -Dtest=ModularityTest
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside outside of package 'com.datiacode.shop.ai.internal..' should depend on classes that reside in a package 'org.springframework.ai..', because the AI provider is an internal detail of the ai module; use ai.TextWriter instead' was violated (6 times):
Constructor <com.datiacode.shop.notification.internal.ShortcutWriter.<init>(org.springframework.ai.chat.client.ChatClient$Builder)> calls method <org.springframework.ai.chat.client.ChatClient$Builder.build()> in (ShortcutWriter.java:15)
Constructor <com.datiacode.shop.notification.internal.ShortcutWriter.<init>(org.springframework.ai.chat.client.ChatClient$Builder)> has parameter of type <org.springframework.ai.chat.client.ChatClient$Builder> in (ShortcutWriter.java:0)
Field <com.datiacode.shop.notification.internal.ShortcutWriter.chatClient> has type <org.springframework.ai.chat.client.ChatClient> in (ShortcutWriter.java:0)
Method <com.datiacode.shop.notification.internal.ShortcutWriter.write(java.lang.String)> calls method <org.springframework.ai.chat.client.ChatClient$CallResponseSpec.content()> in (ShortcutWriter.java:19)
Method <com.datiacode.shop.notification.internal.ShortcutWriter.write(java.lang.String)> calls method <org.springframework.ai.chat.client.ChatClient$ChatClientRequestSpec.call()> in (ShortcutWriter.java:19)
Method <com.datiacode.shop.notification.internal.ShortcutWriter.write(java.lang.String)> calls method <org.springframework.ai.chat.client.ChatClient.prompt(java.lang.String)> in (ShortcutWriter.java:19)
```

**Falla**, y señala cada uso con su línea: el parámetro del constructor, el campo y cada llamada a la API fluida.

## 3. Sin el atajo (estado de `main`)

```text
$ ./mvnw test -Dtest=ModularityTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Conclusión

- La hipótesis se confirma y el Post 9 es correcto: `verify()` protege las fronteras **entre nuestros módulos**, no el uso de librerías.
- Para que una librería solo se use desde un módulo hace falta una regla propia. ArchUnit ya está en el classpath, porque Spring Modulith lo usa por debajo.
- La regla no es exclusiva de la IA: sirve para cualquier dependencia que quieras confinar en un módulo (un cliente de Kafka, un SDK de pagos...).
