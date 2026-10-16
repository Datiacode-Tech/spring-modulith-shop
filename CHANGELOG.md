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

### Changed
- (sin entradas)

### Deprecated
- (sin entradas)

### Removed
- (sin entradas)

### Fixed
- (sin entradas)

### Security
- (sin entradas)
