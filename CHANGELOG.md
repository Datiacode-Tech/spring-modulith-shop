# Changelog

Todas las modificaciones notables de este proyecto se registran aquí.
Sigue [Keep a Changelog](https://keepachangelog.com/) y este proyecto
usa [Semantic Versioning](https://semver.org/).

El plan-development que origina cada entrada se referencia con su
F-id (ver `PLAN.md`). Cada bullet incluye `[YYYY-MM-DD HH:MM]` + `(F-id)` + descripción.

## [Unreleased]

### Added
- **[2026-10-16 13:09]** (F0-01) Esqueleto Maven Spring Boot 4.1.1 / Java 21 generado con Initializr, con BOM de Spring Modulith 2.1.1 y Spring AI 2.0.1, springdoc 3.1.1 y Maven wrapper; `./mvnw compile` en verde.
- **[2026-10-16 13:14]** (F0-02) `docker-compose.yml` (proyecto `shop`) con PostgreSQL 16 en el puerto 5442 y healthcheck; `.env`/`.env.example` y perfil `dev` que lee `.env`; `/actuator/health` en `UP`.
- **[2026-10-16 13:14]** (F0-03) Configuración base y perfiles `dev`/`test`/`prod`, Flyway con `V1__baseline.sql` y `V2__event_publication.sql` (adelantada de F2-04: `spring-modulith-starter-jpa` exige la tabla con `ddl-auto=validate`), `logback-spring.xml` con rotación en `prod` y `.gitignore` ampliado.
- **[2026-10-16 13:14]** (F0-04) `README.md` inicial: qué es, stack con versiones, arranque rápido, tests, perfiles y tabla de tags.
- **[2026-10-16 13:19]** (F1-01) Módulo `inventory`: API pública `InventoryService.isAvailable(sku, quantity)`, implementación y entidad `Product` package-private en `internal`, migración `V3__products.sql` con 4 productos de ejemplo y `DefaultInventoryServiceTest` (4 casos en verde).
- **[2026-10-16 13:25]** (F1-04) Named interface `inventory::catalog` con `ProductInfo` e `InventoryService.findProduct(sku)`; 2 casos nuevos en `DefaultInventoryServiceTest`.
- **[2026-10-16 13:25]** (F1-02) Módulo `order`: API `OrderService` (`create`, `findById`) con records `NewOrder`/`OrderDetails`, excepciones de dominio públicas, entidad `Order` con líneas embebidas, migración `V4__orders.sql` y consulta síncrona de stock a `inventory`; `DefaultOrderServiceTest` (3 casos).
- **[2026-10-16 13:25]** (F1-03) `POST /orders` y `GET /orders/{id}` con DTOs, `OrderExceptionHandler` del módulo (409/404) y `GlobalExceptionHandler` en `shared.web` (400) con `ProblemDetail`; `OrderControllerTest` (5 casos) y Swagger UI operativo.
- **[2026-10-16 13:25]** (F1-05) `ModularityTest` con `ApplicationModules.verify()` y comprobación de la named interface `catalog`.
- **[2026-10-16 13:25]** (F1-07) `DocumentationTest` (tag `documentation`, perfil Maven `docs`) que genera diagramas C4/PlantUML, canvases y `modules.txt` en `docs/modules/`.
- **[2026-10-16 13:26]** (F1-06) Rama `demo/verify-roto` con acceso ilegal `order` → `inventory.internal.ProductRepository` (`ModularityTest` falla con 4 violaciones; en `main` pasa), salida real en `docs/verify-error.txt` y parche en `docs/demo-verify-roto.patch`.
- **[2026-10-16 13:26]** (F1-08) README con la sección "Módulos y fronteras" (estructura, tabla de módulos, `verify()`, documentación generada, API) y tabla de tests; tag `blog-1-modulos`.
- **[2026-10-16 13:27]** (F2-01) Evento público `OrderCompleted` publicado con `ApplicationEventPublisher` dentro de la transacción de `DefaultOrderService.create`; `DefaultOrderServiceTest` comprueba que se publica uno por pedido aceptado y ninguno si se rechaza.
- **[2026-10-16 13:38]** (F2-02) `OrderCompletedListener` (`@ApplicationModuleListener`) en `inventory`: descuenta stock y registra un WARN si falta, sin fallar; `@EnableAsync` con el executor de Boot (`shop-async-*`); `OrderCompletedListenerTest` (3 casos) e `InventoryModuleTests` con `@ApplicationModuleTest` + `Scenario`.
- **[2026-10-16 13:38]** (F2-03) Módulo `notification` con `OrderConfirmationListener` y `EmailSender` (log); `NotificationModuleTests` con `Scenario` y `EmailSender` mockeado; `order` sigue sin conocer a sus oyentes.
- **[2026-10-16 13:38]** (F2-04) Event Publication Registry comprobado con la app real: una fila por listener en `event_publication`, ambas `COMPLETED` tras un pedido.
- **[2026-10-16 13:38]** (F2-05) Republicación al reiniciar (`republish-outstanding-events-on-restart=true`) y `EventRepublicationTests` con dos contextos sobre el mismo PostgreSQL.
- **[2026-10-16 13:38]** (F2-06) Perfil `demo` con retardo en el listener de `notification` y guion `docs/demo-caida.md` con la salida real de un `kill -9`: solo se reprocesa el listener pendiente.
- **[2026-10-16 13:39]** (F2-07) README con la sección "Eventos y fiabilidad" (diagrama de secuencia, tipos de dependencia, Event Publication Registry, demos) y tablas de tests, perfiles y tags actualizadas; tag `blog-2-eventos`.
- **[2026-10-16 13:51]** (F3-03) Spring Security con usuarios en memoria `alice`/`bob` (password en `.env`) y HTTP Basic sin sesión; el cliente del pedido es el usuario autenticado; esquema `basicAuth` en OpenAPI; `OrderControllerTest` con 401 y usuario autenticado.
- **[2026-10-16 13:51]** (F3-04) `TraceIdFilter` (MDC + cabecera `X-Trace-Id`) y `ContextPropagationConfig`, que registra el traceId en Micrometer context-propagation para que el decorator que instala Modulith lo propague junto al `SecurityContext`; `ContextPropagationConfigTest` (propagación y limpieza).
- **[2026-10-16 13:51]** (F3-05) `ExternalizationTests`: Redpanda y PostgreSQL con Testcontainers; el pedido llega a `shop.orders.completed` con el `orderId` como key.
- **[2026-10-19 10:46]** (F3-01) Redpanda (puerto 19092) y Redpanda Console en compose. La consola se publica en `localhost:8091`, porque el 8081 lo ocupa otro proyecto local (knowledge-core).
- **[2026-10-19 10:46]** (F3-02) Verificación manual: `POST /orders` como `alice` (201; 401 sin credenciales) publica `OrderCompleted` en `shop.orders.completed` con clave `orderId` y cabecera `__TypeId__`; el listener asíncrono registra el usuario `alice` y el mismo traceId de la petición.
- **[2026-10-19 11:01]** (F3-06) README: sección "Externalización y contexto" (`@Externalized`, Redpanda Console en el 8091, usuarios demo, hallazgo del `ContextPropagatingTaskDecorator`), stack, arranque con `docker compose up -d`, ejemplo de `curl` con Basic Auth y tablas de tests y tags; `./mvnw verify` en verde (28 tests). Tag `blog-3-externalizacion`.
- **[2026-10-19 11:25]** (F4-01) Mailpit 1.31.3 en compose (SMTP 1026, UI 8026) y Ollama 0.34.4 bajo el perfil `ai` (6 CPUs, 4 GB, `NUM_PARALLEL=1`, `MAX_LOADED_MODELS=1`, `KEEP_ALIVE=5m`, volumen `ollama-models`); modelo `llama3.2:3b` (Q4_K_M, 2,02 GB). Medido: email de unas 80 palabras en 10,8 s en frío y 6,4 s en caliente (~15 tok/s, 6 CPUs al 100 %, ~2,5 GB de RAM); resultados en el README.
- **[2026-10-19 11:55]** (F4-02) Módulo `ai`: API pública `TextWriter`, `OrderConfirmationRequest` (tipo propio, sin depender de `order`) y `TextGenerationException`; `ai.internal.ChatClientTextWriter` es la única clase con `ChatClient`: prompt de sistema con reglas, quita el preámbulo y rechaza placeholders o respuestas vacías. Starter `spring-ai-starter-model-ollama`, `llama3.2:3b` con temperatura 0.3, embeddings desactivados y `spring.http.clients.read-timeout=30s`. `ChatClientTextWriterTest` (6 casos, `ChatModel` mockeado bajo un `ChatClient` real); `./mvnw verify` en verde (34 tests). Prueba real contra Ollama: 3 de 3 respuestas válidas, de 4,7 a 8,9 s.
- **[2026-10-19 12:05]** (F4-03) `OrderConfirmationListener` pide el texto a `TextWriter` (mapea `OrderCompleted` → `OrderConfirmationRequest`) y, si recibe `TextGenerationException`, usa el texto fijo; `EmailSender` envía por SMTP con `JavaMailSender` (`spring-boot-starter-mail`; Mailpit en `dev`, `MAIL_*` en `prod`, timeouts de 5 s). Spring AI reintenta 10 veces por defecto (backoff 2 s × 5): se limita a 2 intentos (`spring.ai.retry.*`) para que el respaldo llegue en unos 3 s. El perfil `test` apunta SMTP y Ollama al puerto 1. Tests: `OrderConfirmationListenerTest` (3), `EmailSenderTest` (1) y `NotificationModuleTests` con `TextWriter` mockeado; 38 en verde. Verificado: pedido 8 con texto de IA en Mailpit (~10 s, en frío); pedido 10 sin Ollama con el texto de respaldo en ~3 s; el pedido 9, que quedó en `PROCESSING`, se republicó al reiniciar; 0 publicaciones incompletas.
- **[2026-10-19 12:15]** (F4-04) Perfil `cloud` con OpenRouter mediante `spring-ai-starter-model-openai` (`base-url=https://openrouter.ai/api/v1`, `OPENROUTER_API_KEY` en `.env`, modelo `mistralai/mistral-small-24b-instruct-2501` cambiable con `OPENROUTER_MODEL`). `spring.ai.model.chat` elige el proveedor (`ollama` por defecto); embeddings, imagen, audio y moderación desactivados. El SDK de OpenAI no usa ni el `RestClient` de Boot ni `spring.ai.retry`: timeout de 15 s y 1 reintento. `ChatModelSelectionTests` (2 casos); 40 tests en verde. Verificado con `dev,cloud`: pedido 11, email en Mailpit en ~2 s.
- **[2026-10-19 12:25]** (F4-05) `OrderModuleTests`: solo `order` arrancado y `CatalogService` como `@MockitoBean`; `Scenario.stimulate(...).andWaitForEventOfType(OrderCompleted.class)` comprueba id, líneas y total. `@Modulithic(sharedModules = "shared")` en `ShopApplication`: el `Clock`, la seguridad y el tracing se arrancan en todos los tests de módulo. `inventory` y `notification` ya tenían su test con `Scenario` (F2 y F4-03). Los tests no dependen de Ollama ni de Redpanda: el perfil `test` apunta Ollama al puerto 1 y desactiva la externalización. 41 tests en verde.
- **[2026-10-19 12:40]** (F4-06) Experimento: con `ChatClient` inyectado en `notification`, `ApplicationModules.verify()` pasa (Spring AI es una librería, no un módulo). Regla ArchUnit nueva en `ModularityTest` (`onlyAiInternalUsesSpringAi`): solo `ai.internal` puede depender de `org.springframework.ai..`; con el atajo falla y señala 6 usos, sin él pasa. Salida real en `docs/verify-chatclient.md`. Se hizo con una clase temporal en el working tree en lugar de una rama, para no hacer commit de la fase 4 a medias. El Post 9 es correcto en este punto. 42 tests en verde.
- **[2026-10-19 12:55]** (F4-07) README final: arranque con el flujo completo (pedido → log, Mailpit, Redpanda Console y Swagger), comandos por tipo de test y convención `*Test` (sin Docker) / `*Tests` (Testcontainers), módulo `ai` y `shared` en el árbol y la tabla de módulos, perfil `cloud` y perfil de compose `ai`, mapa post ↔ código, demos, troubleshooting y siguiente paso hacia microservicios. Puerto de Redpanda Console configurable (`REDPANDA_CONSOLE_PORT`). `docs/modules/` regenerado con `ai` y `shared`.
- **[2026-10-19 12:40]** (F4-08) Verificación end-to-end sobre un `git clone` limpio del commit de la fase 4, siguiendo el README (con el `.env` local para reutilizar los contenedores en marcha): `clean verify` con 42 tests; `-Pdocs` con 6 módulos e idéntico a lo commiteado; pedido 12 → 201, stock descontado, email de IA en Mailpit, offset del topic de 8 a 9, 3/3 publicaciones completadas, mismo traceId y usuario `alice` en la petición y en los listeners; 401 sin credenciales, 422 con SKU desconocido, 201 + WARN con producto sin stock; demo de caída reproducida (pedido 14: solo se reprocesa `notification`). `docs/demo-caida.md` actualizado (`curl` con Basic Auth, 3 publicaciones por pedido, salida real) y checklist del plan corregida (6 módulos; sin stock → 201). Tag `blog-4-ia-testing`.

### Changed
- **[2026-10-16 13:32]** (F1-09) Separado el módulo `catalog` (nombre y precio, named interface `catalog::model`) de `inventory` (solo stock, tabla `inventory_items`) para evitar el ciclo `order ↔ inventory`; `order` solo depende de `catalog`, un SKU desconocido devuelve 422 (`UnknownProductException`) y el stock pasa a gestionarse por eventos. V3 reescrita, tests, demo `verify-roto`, `docs/` y README actualizados.

### Deprecated
- (sin entradas)

### Removed
- (sin entradas)

- **[2026-10-19 11:01]** (F3-06) `SecurityConfig` añade `Referrer-Policy: strict-origin-when-cross-origin` (Spring Security no la pone por defecto); cubierto en `OrderControllerTest`.

### Fixed
- (sin entradas)

### Security
- (sin entradas)
