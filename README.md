# Spring Modulith Shop

Proyecto de ejemplo de un **monolito modular** con [Spring Modulith](https://spring.io/projects/spring-modulith): una tienda mínima donde cada módulo tiene fronteras claras, se comunica por eventos y puede probarse por separado.

Acompaña a una serie de posts de LinkedIn y a 4 artículos del [blog de DatIACode](https://www.datiacode.com/blog). Cada artículo apunta a un tag de git con el estado exacto del código que explica.

> **Qué no es:** un e-commerce completo. No hay frontend, pagos ni catálogo con búsqueda; solo lo justo para que los conceptos de Modulith tengan un escenario creíble.

## Stack

| Pieza | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Modulith | 2.1.1 |
| Spring AI | 2.0.1 |
| PostgreSQL | 16 (Docker) |
| Flyway | gestionado por Spring Boot |
| springdoc-openapi | 3.1.1 |

## Requisitos

- JDK 21
- Docker + Docker Compose
- No hace falta Maven instalado: el proyecto incluye `./mvnw`.

## Arranque rápido

```bash
# 1. Variables locales (credenciales de desarrollo)
cp .env.example .env

# 2. Base de datos (PostgreSQL en el puerto 5442)
docker compose up -d postgres

# 3. Aplicación con el perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Comprobación
curl localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Flyway crea el esquema al arrancar. Hibernate solo lo valida (`ddl-auto=validate`).

## Tests

```bash
./mvnw test
```

Los tests de integración levantan PostgreSQL con Testcontainers, así que Docker tiene que estar en marcha. No necesitan el `docker compose` de desarrollo.

## Perfiles

| Perfil | Uso |
|---|---|
| `dev` | Desarrollo local contra los servicios de `docker-compose.yml`; lee `.env` |
| `test` | Tests; la base de datos llega por Testcontainers |
| `prod` | Variables de entorno `DATABASE_*`, sin Swagger, log a fichero con rotación |

## Tags y artículos

| Tag | Artículo | Contenido |
|---|---|---|
| `blog-1-modulos` | Fundamentos | *(pendiente)* |
| `blog-2-eventos` | Eventos y fiabilidad | *(pendiente)* |
| `blog-3-externalizacion` | Salir del monolito | *(pendiente)* |
| `blog-4-ia-testing` | IA, testing y camino a microservicios | *(pendiente)* |

## Estado

En construcción. El plan de desarrollo está en [`PLAN.md`](PLAN.md) y el registro de cambios en [`CHANGELOG.md`](CHANGELOG.md).
