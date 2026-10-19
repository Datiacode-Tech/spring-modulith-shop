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
| Redpanda (API de Kafka) + Redpanda Console | 24.2.4 / 2.7.2 (Docker) |
| Spring Security | gestionado por Spring Boot |
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

# 2. Infraestructura: PostgreSQL (5442), Redpanda (19092) y su consola (8091)
docker compose up -d

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

Los tests de integración levantan PostgreSQL y Redpanda con Testcontainers, así que Docker tiene que estar en marcha. No necesitan el `docker compose` de desarrollo.

| Test | Qué cubre |
|---|---|
| `ModularityTest` | Fronteras entre módulos (`verify()`), named interfaces y que `order` no dependa de `inventory` |
| `DefaultCatalogServiceTest`, `DefaultInventoryServiceTest`, `DefaultOrderServiceTest` | Lógica de negocio (JUnit 5 + Mockito) |
| `OrderControllerTest` | Capa HTTP con `@WebMvcTest`: 201/400/401/404/422, cliente tomado del usuario autenticado y cabeceras de seguridad |
| `OrderCompletedListenerTest` | Listener de stock, incluida la falta de stock |
| `InventoryModuleTests`, `NotificationModuleTests` | Un solo módulo arrancado con `@ApplicationModuleTest`, estimulado con `Scenario.publish(...)` |
| `EventRepublicationTests` | Republicación de eventos incompletos tras reiniciar |
| `ExternalizationTests` | `OrderCompleted` llega al topic `shop.orders.completed` (Redpanda con Testcontainers) |
| `ContextPropagationConfigTest` | El usuario y el traceId llegan a otro hilo, y el hilo del pool queda limpio después |
| `ShopApplicationTests` | Arranque completo contra PostgreSQL (Testcontainers) |
| `DocumentationTest` | Genera `docs/modules/` (solo con `-Pdocs`) |

## Módulos y fronteras

Spring Modulith trata cada **paquete directo** bajo `com.datiacode.shop` como un módulo. Lo que está en la raíz del módulo es su API pública; lo que está en `internal` no lo puede usar ningún otro módulo.

```
com.datiacode.shop
 ├── ShopApplication.java
 ├── catalog
 │    ├── CatalogService.java            ← API pública
 │    ├── model                          ← @NamedInterface("model")
 │    │    └── ProductInfo.java
 │    └── internal                       ← oculto: Product, ProductRepository, DefaultCatalogService
 ├── inventory
 │    ├── InventoryService.java          ← API pública (consulta de stock)
 │    └── internal                       ← oculto: StockItem, StockItemRepository, DefaultInventoryService
 ├── notification
 │    └── internal                       ← oculto: OrderConfirmationListener, EmailSender
 ├── order
 │    ├── OrderService.java              ← API pública
 │    ├── NewOrder.java, OrderDetails.java
 │    ├── OrderCompleted.java            ← evento público
 │    ├── UnknownProductException.java, OrderNotFoundException.java
 │    └── internal                       ← oculto: Order, OrderRepository, OrderController, ...
 └── shared
      ├── config                         ← beans transversales (Clock, @EnableAsync)
      └── web                            ← GlobalExceptionHandler (errores del framework)
```

| Módulo | Expone | Depende de |
|---|---|---|
| `catalog` | `CatalogService`, `model::ProductInfo` | — |
| `inventory` | `InventoryService` | `order` (escucha `OrderCompleted`) |
| `order` | `OrderService`, `OrderCompleted`, records y excepciones de dominio | `catalog` (consulta síncrona de nombre y precio) |
| `notification` | — | `order` (escucha `OrderCompleted`) |
| `shared` | — | — |

**Llamar o avisar.** `order → catalog` es una llamada síncrona: sin nombre y precio no se puede guardar el pedido. Lo que pasa *después* (descontar stock, avisar al cliente) va por eventos: `order` publica `OrderCompleted` y no conoce a quien lo escucha.

**Por qué `catalog` e `inventory` son módulos distintos.** El primer diseño tenía un solo `inventory` con precio y stock, y `order` le consultaba el stock antes de aceptar el pedido. Pero si además `inventory` escucha `OrderCompleted`, depende de `order`, y el resultado es un ciclo `order ↔ inventory` que `verify()` rechaza. Separar lo que se vende (`catalog`) de lo que hay en el almacén (`inventory`) rompe el ciclo. A cambio, el stock ya no se comprueba antes de aceptar el pedido: es **consistencia eventual**, e `inventory` gestiona la falta de stock cuando procesa el evento. `ModularityTest.orderDoesNotDependOnInventory` vigila que siga así.

**Doble protección.** Las clases de `internal` son package-private, así que Java ya impide la mayoría de accesos ilegales. Cuando una clase interna tiene que ser pública (algo habitual en proyectos reales), quien vigila la frontera es `ApplicationModules.verify()`.

### Verificar la estructura

```bash
./mvnw test -Dtest=ModularityTest
```

La rama `demo/verify-roto` añade a propósito un acceso de `order` a `catalog.internal.ProductRepository`. En esa rama el test falla con:

```
- Module 'order' depends on non-exposed type com.datiacode.shop.catalog.internal.ProductRepository within module 'catalog'!
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
| `POST` | `/orders` | 201 pedido creado · 400 petición inválida · 401 sin credenciales · 422 SKU que no existe en el catálogo |
| `GET` | `/orders/{id}` | 200 · 404 |

```bash
curl -u alice:change-me -X POST localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"items":[{"sku":"KB-001","quantity":1},{"sku":"MS-001","quantity":2}]}'
```

Swagger UI: <http://localhost:8080/swagger-ui/index.html>. Productos de ejemplo: `KB-001`, `MS-001`, `MN-001` y `HD-001` (este último con stock 0, para ver cómo reacciona `inventory`).

## Eventos y fiabilidad

Cuando se acepta un pedido, `order` publica `OrderCompleted` y se desentiende. Quien esté interesado lo escucha:

```mermaid
sequenceDiagram
    actor Cliente
    participant order
    participant DB as PostgreSQL
    participant inventory
    participant notification

    Cliente->>order: POST /orders
    order->>DB: guarda pedido + una fila por listener en event_publication (misma transacción)
    order-->>Cliente: 201 Created
    DB-->>inventory: OrderCompleted (async, tras el commit)
    inventory->>inventory: descuenta stock (o WARN si falta)
    DB-->>notification: OrderCompleted (async, tras el commit)
    notification->>notification: envía email (de momento, al log)
```

```java
// order: publica dentro de la transacción del pedido
events.publishEvent(toEvent(order));

// inventory y notification: cada uno en su hilo y su transacción
@ApplicationModuleListener
void on(OrderCompleted event) { ... }
```

| Módulo | Dependencia | Tipo |
|---|---|---|
| `order` | `catalog` | `USES_COMPONENT` (llamada síncrona) |
| `inventory` | `order` | `EVENT_LISTENER` |
| `notification` | `order` | `EVENT_LISTENER` |

`@ApplicationModuleListener` combina `@TransactionalEventListener` (solo tras el commit), `@Async` (otro hilo, el executor `shop-async-*`) y `@Transactional(REQUIRES_NEW)` (transacción propia).

### Event Publication Registry

Cada publicación queda registrada en la tabla `event_publication` (migración `V2`), con una fila por listener y su estado (`PUBLISHED`, `PROCESSING`, `COMPLETED`, `FAILED`...). Con `spring.modulith.events.republish-outstanding-events-on-restart=true`, las filas que no llegaron a completarse se reenvían al arrancar.

- **Demo manual:** [`docs/demo-caida.md`](docs/demo-caida.md). Perfil `demo` + `kill -9` a mitad del listener de `notification`; al reiniciar solo se reprocesa ese listener.
- **Demo automática:** `EventRepublicationTests`. Dos contextos arrancados uno tras otro contra el mismo PostgreSQL.
- **Entrega al menos una vez:** un listener puede ejecutarse más de una vez, así que tiene que ser idempotente.

```sql
SELECT substring(listener_id from '([A-Za-z]+Listener)') AS listener, status, completion_date
FROM event_publication ORDER BY publication_date DESC LIMIT 4;
```

## Externalización y contexto

### Eventos hacia fuera: `@Externalized`

`OrderCompleted` sigue siendo un evento interno, pero además sale a Kafka (aquí, Redpanda) sin escribir ni un `KafkaTemplate`:

```java
@Externalized("shop.orders.completed::#{#this.orderId().toString()}")
public record OrderCompleted(Long orderId, String customer, List<Line> lines, BigDecimal total, Instant completedAt) { ... }
```

- Lo que va antes de `::` es el topic y lo de después, la clave del mensaje (el id del pedido; así todos los mensajes de un pedido caen en la misma partición).
- La publicación hacia Kafka pasa por el mismo Event Publication Registry que los listeners internos: si el broker está caído, esa fila queda incompleta y se reenvía al reiniciar, igual que la de cualquier listener.
- El mensaje lleva la cabecera `__TypeId__` con el tipo Java del evento.

Para verlo:

1. Crea un pedido con el `curl` de la sección [API](#api).
2. Abre Redpanda Console en <http://localhost:8091> → *Topics* → `shop.orders.completed`.

> La consola usa el 8091 en el host porque el 8081 suele estar ocupado. Si lo cambias, edita `docker-compose.yml`.

### Usuarios demo

| Usuario | Contraseña | Rol |
|---|---|---|
| `alice` | `DEMO_PASSWORD` de `.env` (`change-me` en `.env.example`) | `CUSTOMER` |
| `bob` | la misma | `CUSTOMER` |

HTTP Basic y sin sesión. El cliente del pedido es siempre el usuario autenticado: si el body trae un `customer`, se ignora. `/actuator/health/**` y Swagger son públicos.

### El contexto llega al listener asíncrono

Un `@ApplicationModuleListener` corre en otro hilo (`shop-async-*`), así que lo que vive en `ThreadLocal` (el `SecurityContext` y el MDC) no viaja solo. En el log de un pedido real se ve que sí llega:

```text
INFO [75d254c91be64cef] ... [shop-async-3] c.d.s.n.i.OrderConfirmationListener : Order 7 confirmation requested by user alice
```

`[75d254c91be64cef]` es el traceId de la petición: `TraceIdFilter` lo toma de la cabecera `X-Trace-Id` (o lo genera) y lo devuelve en la respuesta.

**Hallazgo:** no hace falta, y no sirve, un `TaskDecorator` propio.

- `spring-modulith-observability` instala en el executor el `ContextPropagatingTaskDecorator` de Spring, que **sustituye en silencio** cualquier `TaskDecorator` que registres tú.
- Ese decorator propaga todo lo registrado en el `ContextRegistry` de Micrometer. Spring Security ya registra el `SecurityContext`; el MDC, no.
- Por eso `ContextPropagationConfig` solo registra un accessor para la clave `traceId` del MDC. El decorator también restaura el estado anterior del hilo del pool tras cada tarea, así que no se cuelan datos de otra petición.

## Perfiles

| Perfil | Uso |
|---|---|
| `dev` | Desarrollo local contra los servicios de `docker-compose.yml`; lee `.env` |
| `test` | Tests; la base de datos llega por Testcontainers |
| `demo` | Combinado con `dev`: pausa de 20 s en el listener de `notification` para la demo de caída |
| `prod` | Variables de entorno `DATABASE_*`, sin Swagger, log a fichero con rotación |

## Tags y artículos

| Tag | Artículo | Contenido |
|---|---|---|
| `blog-1-modulos` | Fundamentos | Módulos `catalog`, `inventory` y `order`, API vs `internal`, `@NamedInterface`, `verify()`, `Documenter` |
| `blog-2-eventos` | Eventos y fiabilidad | `OrderCompleted`, listeners en `inventory` y `notification`, Event Publication Registry, republicación y demo de caída |
| `blog-3-externalizacion` | Salir del monolito | `@Externalized` hacia Redpanda, Spring Security y propagación del `SecurityContext` y del traceId a los listeners asíncronos |
| `blog-4-ia-testing` | IA, testing y camino a microservicios | *(pendiente)* |

## Estado

En construcción. El plan de desarrollo está en [`PLAN.md`](PLAN.md) y el registro de cambios en [`CHANGELOG.md`](CHANGELOG.md).
