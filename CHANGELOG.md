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
