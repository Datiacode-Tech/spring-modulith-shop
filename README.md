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

| Test | Qué cubre |
|---|---|
| `ModularityTest` | Fronteras entre módulos (`verify()`) y named interfaces |
| `DefaultInventoryServiceTest`, `DefaultOrderServiceTest` | Lógica de negocio (JUnit 5 + Mockito) |
| `OrderControllerTest` | Capa HTTP con `@WebMvcTest`: 201/400/404/409 |
| `ShopApplicationTests` | Arranque completo contra PostgreSQL (Testcontainers) |
| `DocumentationTest` | Genera `docs/modules/` (solo con `-Pdocs`) |

## Módulos y fronteras

Spring Modulith trata cada **paquete directo** bajo `com.datiacode.shop` como un módulo. Lo que está en la raíz del módulo es su API pública; lo que está en `internal` no lo puede usar ningún otro módulo.

```
com.datiacode.shop
 ├── ShopApplication.java
 ├── inventory
 │    ├── InventoryService.java          ← API pública
 │    ├── catalog                        ← @NamedInterface("catalog")
 │    │    └── ProductInfo.java
 │    └── internal                       ← oculto: Product, ProductRepository, DefaultInventoryService
 ├── order
 │    ├── OrderService.java              ← API pública
 │    ├── NewOrder.java, OrderDetails.java
 │    ├── InsufficientStockException.java, OrderNotFoundException.java
 │    └── internal                       ← oculto: Order, OrderRepository, OrderController, ...
 └── shared
      ├── config                         ← beans transversales (Clock)
      └── web                            ← GlobalExceptionHandler (errores del framework)
```

| Módulo | Expone | Depende de |
|---|---|---|
| `inventory` | `InventoryService`, `catalog::ProductInfo` | — |
| `order` | `OrderService`, records y excepciones de dominio | `inventory` (consulta síncrona de stock) |
| `shared` | — | — |

**Llamar o avisar.** `order → inventory` es una llamada síncrona porque hay que saber si hay stock *antes* de aceptar el pedido. Lo que pasa *después* de aceptarlo irá por eventos (fase 2).

**Doble protección.** Las clases de `internal` son package-private, así que Java ya impide la mayoría de accesos ilegales. Cuando una clase interna tiene que ser pública (algo habitual en proyectos reales), quien vigila la frontera es `ApplicationModules.verify()`.

### Verificar la estructura

```bash
./mvnw test -Dtest=ModularityTest
```

La rama `demo/verify-roto` añade a propósito un acceso de `order` a `inventory.internal.ProductRepository`. En esa rama el test falla con:

```
- Module 'order' depends on non-exposed type com.datiacode.shop.inventory.internal.ProductRepository within module 'inventory'!
Field <com.datiacode.shop.order.internal.StockShortcut.products> has type <...ProductRepository> in (StockShortcut.java:0)
```

La salida completa está en [`docs/verify-error.txt`](docs/verify-error.txt).

### Documentación generada

```bash
./mvnw test -Pdocs
```

Genera en [`docs/modules/`](docs/modules/) el diagrama de componentes C4 (`components.puml`), un diagrama y un canvas por módulo, y `modules.txt` con las named interfaces. Los canvases de Modulith 2.1.1 no las muestran. El test lleva el tag `documentation` y queda fuera del build normal.

### API

| Método | Ruta | Respuestas |
|---|---|---|
| `POST` | `/orders` | 201 pedido creado · 400 petición inválida · 409 sin stock o SKU desconocido |
| `GET` | `/orders/{id}` | 200 · 404 |

```bash
curl -X POST localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"customer":"alice","items":[{"sku":"KB-001","quantity":1},{"sku":"MS-001","quantity":2}]}'
```

Swagger UI: <http://localhost:8080/swagger-ui/index.html>. Productos de ejemplo: `KB-001`, `MS-001`, `MN-001` y `HD-001` (este último sin stock, para provocar el 409).

## Perfiles

| Perfil | Uso |
|---|---|
| `dev` | Desarrollo local contra los servicios de `docker-compose.yml`; lee `.env` |
| `test` | Tests; la base de datos llega por Testcontainers |
| `prod` | Variables de entorno `DATABASE_*`, sin Swagger, log a fichero con rotación |

## Tags y artículos

| Tag | Artículo | Contenido |
|---|---|---|
| `blog-1-modulos` | Fundamentos | Módulos `order` e `inventory`, API vs `internal`, `@NamedInterface`, `verify()`, `Documenter` |
| `blog-2-eventos` | Eventos y fiabilidad | *(pendiente)* |
| `blog-3-externalizacion` | Salir del monolito | *(pendiente)* |
| `blog-4-ia-testing` | IA, testing y camino a microservicios | *(pendiente)* |

## Estado

En construcción. El plan de desarrollo está en [`PLAN.md`](PLAN.md) y el registro de cambios en [`CHANGELOG.md`](CHANGELOG.md).
