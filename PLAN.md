# Spring Modulith Shop — development plan

## Contexto

Construir el repositorio de ejemplo **Spring Modulith Shop**: una tienda mínima (sin frontend, pagos ni catálogo) que sirva de referencia de código real para los 9 posts de LinkedIn ya escritos (`../posts/`) y los 4 artículos del blog de DatIACode. Cada concepto de los posts tiene que poder señalarse en una clase o un test concretos.

Fuente del diseño: `../proyecto-ejemplo-spring-modulith.md` (dominio, módulos, mapa post ↔ código, stack). Convenciones: `~/.claude/CLAUDE.md` (capas controller → service → repository → DTO, inyección por constructor, `@RestControllerAdvice`, JUnit 5 + Mockito + Testcontainers, código en inglés y documentación en español).

Stack: Java 21, Maven con un solo módulo, Spring Boot + Spring Modulith + Spring AI (última versión estable compatible entre sí, se fija en F0-01), Spring Data JPA + Flyway, PostgreSQL, **Redpanda** (compatible con Kafka) + Redpanda Console, Ollama (modelo ~3B, el NUC no tiene GPU) y Mailpit. Paquete raíz: `com.datiacode.shop`.

Resultado esperado: un repo git local (sin remoto; la ubicación en GitHub se decide más adelante) con 4 tags, uno por artículo (`blog-1-modulos`, `blog-2-eventos`, `blog-3-externalizacion`, `blog-4-ia-testing`). Cada tag compila y deja todos los tests en verde. El `README.md` se crea en F0 y se va ampliando en cada fase; queda completo en F4.

## Convenciones

- **IDs**: `F<N>-<NN>`. F0 = setup; F1..F4 = una fase por artículo del blog.
- **Checkbox**: `[ ]` abierto; `[x]` cerrado cuando su AC está verificado.
- **Prioridad**: P0 bloqueante · P1 crítico para el entregable de la fase/artículo · P2 importante no bloqueante · P3 nice-to-have.
- **Cierre de item**: AC verificado + entrada en `CHANGELOG.md` con formato `[YYYY-MM-DD HH:MM] (F-id) descripción`.
- **Cierre de fase**: `mvn verify` en verde → actualizar el README → commit + tag. Commits y tags **siempre con tu OK previo** (regla del persona).
- **Docker**: `docker compose up/down` y `docker pull` necesitan tu OK. Los tests con Testcontainers también levantan contenedores (ver Issues abiertas).

## Resumen ejecutivo

| ID | Subsistema / área | Prioridad | Objetivo (1 línea) |
|----|-------------------|-----------|---------------------|
| F0-01 | scaffold | P0 | Proyecto Maven Spring Boot con versiones fijadas y app arrancando |
| F0-02 | infra | P0 | `docker-compose.yml` con PostgreSQL y conexión de la app |
| F0-03 | config | P0 | Perfiles, Flyway, logging y `.gitignore` |
| F0-04 | docs | P1 | Esqueleto de `README.md` (qué es, cómo arrancar) |
| F1-01 | inventory | P1 | Módulo `inventory`: `Product`, stock y API pública `InventoryService` |
| F1-02 | order | P1 | Módulo `order`: `Order`, `OrderService`, comprobación síncrona de stock |
| F1-03 | order / api | P1 | `POST /orders`, `GET /orders/{id}` con DTOs y manejo global de errores |
| F1-04 | inventory | P2 | `@NamedInterface` para exponer un subpaquete (Post 2) |
| F1-05 | modularity | P0 | `ModularityTest` con `verify()` |
| F1-06 | modularity | P1 | Rama `demo/verify-roto` con un acceso ilegal y el error capturado |
| F1-07 | docs | P1 | `DocumentationTest` con `Documenter`; diagramas en `docs/modules/` |
| F1-08 | release | P1 | README fase 1 + tag `blog-1-modulos` |
| F2-01 | order | P1 | Evento `OrderCompleted` publicado al completar el pedido |
| F2-02 | inventory | P1 | `@ApplicationModuleListener` que descuenta stock |
| F2-03 | notification | P1 | Módulo `notification` con listener y `EmailSender` (log, de momento) |
| F2-04 | events | P0 | Event Publication Registry JPA (tabla `event_publication`) |
| F2-05 | events | P1 | Republicación al reiniciar + test con Testcontainers |
| F2-06 | events | P2 | Guion de la demo de caída en `docs/demo-caida.md` |
| F2-07 | release | P1 | README fase 2 + tag `blog-2-eventos` |
| F3-01 | infra | P0 | Redpanda + Redpanda Console en compose |
| F3-02 | events | P1 | `@Externalized` de `OrderCompleted` → `shop.orders.completed` |
| F3-03 | security | P1 | Spring Security con usuarios en memoria y Basic Auth |
| F3-04 | observability | P1 | `TaskDecorator` que propaga `SecurityContext` + MDC (traceId) a los listeners |
| F3-05 | events | P2 | Test de externalización con Testcontainers (Redpanda) |
| F3-06 | release | P1 | README fase 3 + tag `blog-3-externalizacion` |
| F4-01 | infra | P0 | Ollama + Mailpit en compose; modelo ~3B |
| F4-02 | ai | P1 | Módulo `ai`: `TextWriter` + `ChatClientTextWriter` |
| F4-03 | notification | P1 | `notification` usa `TextWriter` y envía por SMTP a Mailpit |
| F4-04 | ai / config | P2 | Perfil alternativo con proveedor en la nube |
| F4-05 | testing | P1 | `@ApplicationModuleTest` + `Scenario` en cada módulo |
| F4-06 | modularity | P1 | Comprobar si `verify()` detecta `ChatClient` fuera de `ai`; regla ArchUnit si no lo hace |
| F4-07 | docs | P1 | README final completo |
| F4-08 | release | P1 | Verificación end-to-end + tag `blog-4-ia-testing` |

## Fase 0 — Setup / scaffolding

### `[x]` F0-01 · scaffold · **P0**

**Objetivo.** Esqueleto Maven arrancable con versiones de Spring Boot, Spring Modulith y Spring AI compatibles entre sí. Todo lo demás depende de esto.

**Pasos.**
1. Consultar start.spring.io / Maven Central y fijar la última versión estable de Spring Boot que soporten a la vez Spring Modulith y Spring AI. Las versiones se anotan en el README.
2. Crear `pom.xml` (Java 21) con los BOM `spring-modulith-bom` y `spring-ai-bom`, más web, data-jpa, validation, actuator, postgresql, flyway, springdoc-openapi, modulith-starter-core/jpa/test, testcontainers y junit.
3. Crear `com.datiacode.shop.ShopApplication` y el Maven wrapper (`mvnw`).

**AC.**
- `./mvnw -q compile` termina sin errores.
- Las versiones fijadas aparecen como propiedades en `pom.xml`.

### `[x]` F0-02 · infra · **P0**

**Objetivo.** PostgreSQL local con Docker Compose para desarrollo; la app se conecta a él.

**Pasos.**
1. Crear `docker-compose.yml` con `postgres` en el puerto host `5442` (volumen con nombre, healthcheck). El `5432` y el `5433` los usan `dke-postgres` y `dke-education-postgres`. Nombre de proyecto compose `shop` para no chocar con contenedores ajenos.
2. Credenciales de desarrollo en `.env` (ignorado por git) y un `.env.example` versionado.
3. Configurar el datasource en el perfil `dev`.

**AC.**
- Con `docker compose up -d postgres` (con tu OK), `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` arranca y `/actuator/health` devuelve `UP`.

### `[x]` F0-03 · config · **P0**

**Objetivo.** Configuración base: perfiles, migraciones, logging y exclusiones de git.

**Pasos.**
1. Crear la configuración base y los perfiles `dev` y `test` (formato pendiente: ver Issues abiertas).
2. Añadir Flyway con `V1__baseline.sql`; `spring.jpa.hibernate.ddl-auto=validate`.
3. Añadir `logback-spring.xml`: consola en `dev`, fichero con rotación en `prod`.
4. Añadir `.gitignore` (target, .env, IDE, logs).

**AC.**
- Al arrancar en `dev`, Flyway aplica `V1` y la app queda `UP`.
- `git status` no lista `.env` ni `target/`.

### `[x]` F0-04 · docs · **P1**

**Objetivo.** README mínimo desde el primer commit, para que el repo se entienda solo.

**Pasos.**
1. Crear `README.md` (en español): qué es, a qué posts/artículos acompaña, requisitos, cómo arrancar, tabla de tags (vacía al inicio).

**AC.**
- Siguiendo el README desde cero se llega a `/actuator/health` = `UP`.

## Fase 1 — Módulos y fronteras (`blog-1-modulos` · Posts 1–4)

### `[x]` F1-01 · inventory · **P1**

**Objetivo.** Módulo `inventory` con stock consultable desde fuera solo a través de su API pública.

**Pasos.**
1. Crear `inventory/internal/Product` (JPA), `ProductRepository` y la migración `V3__products.sql` con productos de ejemplo.
2. Crear la API pública `inventory/InventoryService` con `boolean isAvailable(String sku, int quantity)`; su implementación va en `internal`.
3. Escribir el test unitario `InventoryServiceTest` (Mockito).

**AC.**
- `InventoryServiceTest` en verde (con stock, sin stock, SKU inexistente).

### `[x]` F1-02 · order · **P1**

**Objetivo.** Módulo `order` que acepta un pedido solo si hay stock: es la llamada síncrona del Post 2.

**Pasos.**
1. Crear `order/internal/Order` + `OrderLine` (JPA), `OrderRepository` y la migración `V4__orders.sql`.
2. Crear la API pública `order/OrderService` con `create(...)`, que consulta `InventoryService` y lanza una excepción de dominio si falta stock.
3. Escribir el test unitario `OrderServiceTest` (Mockito sobre `InventoryService`).

**AC.**
- `OrderServiceTest` en verde: pedido aceptado y pedido rechazado por falta de stock.

### `[x]` F1-03 · order / api · **P1**

**Objetivo.** Exponer el caso de uso por HTTP siguiendo las convenciones de capas.

**Pasos.**
1. Crear `order/internal/OrderController` con `POST /orders` y `GET /orders/{id}`, y los DTOs `CreateOrderRequest`/`OrderResponse` (record + validation).
2. Crear `order/internal/OrderExceptionHandler` (advice del módulo: falta de stock → 409, no encontrado → 404) y `shared/web/GlobalExceptionHandler` (validación y errores del framework → 400), todo con `ProblemDetail`.
3. Escribir el test `OrderControllerTest` con `@WebMvcTest`.

**AC.**
- `OrderControllerTest` en verde: 201, 400, 404 y 409.
- Swagger UI muestra los dos endpoints.

### `[x]` F1-04 · inventory · **P2**

**Objetivo.** Ilustrar `@NamedInterface` (Post 2): exponer un subpaquete sin hacerlo público entero.

**Pasos.**
1. Crear `inventory/catalog/ProductInfo` (sku, nombre, precio) con `package-info.java` anotado con `@NamedInterface("catalog")`, y `InventoryService.findProduct(sku)`.
2. Hacer que `order` lo use para precio y nombre de las líneas.

**AC.**
- `ModularityTest` (F1-05) sigue en verde y comprueba que existe la interfaz nombrada `catalog`; `docs/modules/modules.txt` la lista (los canvases de esta versión no muestran named interfaces).

### `[x]` F1-05 · modularity · **P0**

**Objetivo.** Test de estructura que falla si alguien cruza una frontera (Post 3). Es la red de seguridad de todas las fases siguientes.

**Pasos.**
1. Crear `ModularityTest` con `ApplicationModules.of(ShopApplication.class).verify()`.

**AC.**
- `./mvnw test -Dtest=ModularityTest` en verde.

### `[x]` F1-06 · modularity · **P1**

**Objetivo.** Material real para el Post 3 y el artículo 1: el error que da `verify()` ante un acceso ilegal.

**Pasos.**
1. Crear la rama `demo/verify-roto` con un acceso de `order` a `inventory.internal.ProductRepository` (parche en `docs/demo-verify-roto.patch`; hace públicos `Product` y `ProductRepository`, porque al ser package-private ni compilaría).
2. Ejecutar `ModularityTest` y guardar la salida del error en `docs/verify-error.txt` en `main`.
3. Volver a `main`; la rama queda como demo.

**AC.**
- En la rama, `ModularityTest` falla con el mensaje de violación; en `main` pasa.
- `docs/verify-error.txt` contiene la salida real.

### `[x]` F1-07 · docs · **P1**

**Objetivo.** Documentación viva generada desde el código (Post 4).

**Pasos.**
1. Crear `DocumentationTest` (`@Tag("documentation")`) con `new Documenter(modules).writeDocumentation()`.
2. Excluir ese tag del build por defecto; ejecutarlo con un perfil Maven `docs` y copiar la salida a `docs/modules/`.

**AC.**
- `./mvnw test -Pdocs` genera diagramas PlantUML y module canvases de `order` e `inventory` en `docs/modules/`.
- `./mvnw test` sin el perfil no los regenera.

### `[x]` F1-08 · release · **P1**

**Objetivo.** Cerrar la fase 1 con un estado citable desde el artículo 1.

**Pasos.**
1. Añadir al README la sección "Módulos y fronteras": estructura, tabla de módulos, `verify()`, diagramas, rama demo.
2. Con tu OK: commit y tag `blog-1-modulos`.

**AC.**
- `./mvnw verify` en verde.
- `git tag` lista `blog-1-modulos`.

## Fase 2 — Eventos y fiabilidad (`blog-2-eventos` · Posts 5–6)

### `[ ]` F2-01 · order · **P1**

**Objetivo.** `order` avisa de que algo pasó sin saber quién escucha (Post 5).

**Pasos.**
1. Crear el record público `order/OrderCompleted(orderId, customer, lines, total, completedAt)`.
2. Publicarlo con `ApplicationEventPublisher` dentro de la transacción de `OrderService.create`.
3. Ampliar `OrderServiceTest` para comprobar la publicación.

**AC.**
- El test comprueba que se publica exactamente un `OrderCompleted` por pedido aceptado.

### `[ ]` F2-02 · inventory · **P1**

**Objetivo.** El stock se descuenta reaccionando al evento, en otra transacción y otro hilo.

**Pasos.**
1. Crear `inventory/internal/OrderCompletedListener` con `@ApplicationModuleListener` que descuenta el stock.
2. Activar `@EnableAsync` en `shared/config` con un executor propio.
3. Escribir un test de integración con `@ApplicationModuleTest` + `Scenario.publish(...)` que verifique el stock final.

**AC.**
- El test confirma que el stock baja tras publicar `OrderCompleted`.

### `[ ]` F2-03 · notification · **P1**

**Objetivo.** Segundo consumidor del mismo evento; demuestra que `order` no cambia al añadir oyentes.

**Pasos.**
1. Crear `notification/internal/OrderConfirmationListener` y `EmailSender` (en esta fase escribe en el log con un texto fijo).
2. Verificar que `order` no tiene ninguna dependencia hacia `notification`.

**AC.**
- `ModularityTest` en verde y el Documenter muestra `notification` → `order` (como dependencia de evento), sin la flecha inversa.

### `[ ]` F2-04 · events · **P0**

**Objetivo.** Persistir cada publicación en `event_publication` dentro de la misma transacción del pedido (Post 6). Sin esto no hay fiabilidad que demostrar.

**Pasos.**
1. ~~Añadir `spring-modulith-starter-jpa` y la migración de `event_publication`~~: ya hecho en F0-03 (`V2__event_publication.sql`). Aquí solo se comprueba con eventos reales.
2. Exponer las publicaciones pendientes vía Actuator (`modulith` endpoint) si la versión lo soporta.

**AC.**
- Tras un pedido, `event_publication` tiene una fila por listener con `completion_date` rellena.

### `[ ]` F2-05 · events · **P1**

**Objetivo.** Probar que un evento cuyo listener falló o no terminó se reprocesa al reiniciar.

**Pasos.**
1. Activar `spring.modulith.events.republish-outstanding-events-on-restart=true`.
2. Escribir el test `EventRepublicationIT` (Testcontainers PostgreSQL): un listener que falla la primera vez deja la publicación incompleta; al re-arrancar el contexto se completa.

**AC.**
- `EventRepublicationIT` en verde.

### `[ ]` F2-06 · events · **P2**

**Objetivo.** Guion reproducible de la demo "matar la app a mitad del evento", para capturas del artículo 2.

**Pasos.**
1. Añadir un retardo configurable en el listener de `notification` (propiedad, solo perfil `demo`).
2. Escribir `docs/demo-caida.md`: crear pedido → `kill -9` → consultar `event_publication` → reiniciar → ver la fila completada.

**AC.**
- Siguiendo el guion se observa la fila incompleta y después completada.

### `[ ]` F2-07 · release · **P1**

**Objetivo.** Cerrar la fase 2.

**Pasos.**
1. Añadir al README la sección "Eventos y fiabilidad", regenerar diagramas.
2. Con tu OK: commit y tag `blog-2-eventos`.

**AC.**
- `./mvnw verify` en verde; `git tag` lista `blog-2-eventos`.

## Fase 3 — Salir del monolito (`blog-3-externalizacion` · Posts 7–8)

### `[ ]` F3-01 · infra · **P0**

**Objetivo.** Broker Kafka local y ligero para externalizar eventos.

**Pasos.**
1. Añadir `redpanda` y `redpanda-console` a `docker-compose.yml` (consola en `localhost:8081`).
2. Añadir `spring-kafka` y `spring-modulith-events-kafka`; configurar `bootstrap-servers` en `dev`.

**AC.**
- Con tu OK, `docker compose up -d` deja Redpanda healthy y la consola accesible.

### `[ ]` F3-02 · events · **P1**

**Objetivo.** El mismo `OrderCompleted` sale a Kafka sin tocar `OrderService` (Post 7).

**Pasos.**
1. Anotar `OrderCompleted` con `@Externalized("shop.orders.completed::#{orderId()}")`.
2. Configurar la serialización JSON.

**AC.**
- Tras `POST /orders`, el mensaje aparece en el topic `shop.orders.completed` en Redpanda Console, con el `orderId` como key.

### `[ ]` F3-03 · security · **P1**

**Objetivo.** Tener un `SecurityContext` real que propagar (Post 8), sin desviarse hacia OAuth.

**Pasos.**
1. Crear `shared/config/SecurityConfig`: usuarios en memoria (`alice`, `bob`), Basic Auth, `/orders/**` autenticado; health y Swagger públicos.
2. Guardar en el pedido el usuario autenticado como `customer`.
3. Actualizar `OrderControllerTest` (`@WithMockUser`, 401 sin credenciales).

**AC.**
- `curl` sin credenciales → 401; con `alice` → 201 y `customer = alice`.

### `[ ]` F3-04 · observability · **P1**

**Objetivo.** Que el listener asíncrono sepa quién hizo el pedido y comparta traceId con la petición (Post 8).

**Pasos.**
1. Crear `shared/config/ContextPropagatingTaskDecorator`, que copia `SecurityContext` y el MDC al hilo del executor y los limpia al terminar.
2. Registrarlo en el executor de F2-02; poner un filtro que meta `traceId` en el MDC por petición (o usar Micrometer Tracing si viene sin coste).
3. Hacer que `OrderConfirmationListener` registre en el log el usuario y el traceId.
4. Escribir un test unitario del decorator.

**AC.**
- El test del decorator verifica la propagación y la limpieza.
- En el log de un pedido real, el traceId de la petición y el del listener coinciden, y aparece `alice`.

### `[ ]` F3-05 · events · **P2**

**Objetivo.** Prueba automática de la externalización.

**Pasos.**
1. Escribir `ExternalizationIT` con Testcontainers (Redpanda + PostgreSQL) y un consumidor de prueba.

**AC.**
- `ExternalizationIT` en verde.

### `[ ]` F3-06 · release · **P1**

**Objetivo.** Cerrar la fase 3.

**Pasos.**
1. Añadir al README la sección "Externalización y contexto", con los usuarios demo y la URL de la consola.
2. Con tu OK: commit y tag `blog-3-externalizacion`.

**AC.**
- `./mvnw verify` en verde; `git tag` lista `blog-3-externalizacion`.

## Fase 4 — IA y testing (`blog-4-ia-testing` · Post 9)

### `[ ]` F4-01 · infra · **P0**

**Objetivo.** LLM local y bandeja de email visible.

**Pasos.**
1. Añadir `mailpit` a `docker-compose.yml` en los puertos `1026`/`8026` (el `1025`/`8025` lo usa `dke-mailpit`).
2. Añadir `ollama` bajo el **compose profile `ai`**, para que no arranque con un `docker compose up` normal. Límites de recursos: `cpus: 6`, `mem_limit: 4g`, `OLLAMA_NUM_PARALLEL=1`, `OLLAMA_MAX_LOADED_MODELS=1`, `OLLAMA_KEEP_ALIVE=5m` (descarga el modelo de la RAM tras 5 min sin uso). Volumen con nombre para los modelos.
3. Con tu OK, descargar `llama3.2:3b` (Q4, ~2 GB; alternativa `qwen2.5:3b`) y medir la latencia de un email corto con los límites puestos.

**AC.**
- `docker compose up -d` no arranca Ollama; `docker compose --profile ai up -d` sí.
- La API de Ollama responde a un prompt de prueba; la latencia y el consumo (`docker stats`) quedan anotados en el README.

### `[ ]` F4-02 · ai · **P1**

**Objetivo.** La IA como un módulo más: interfaz pública propia, Spring AI encapsulado (Post 9).

**Pasos.**
1. Crear la API pública `ai/TextWriter` con `String writeOrderConfirmation(OrderConfirmationRequest)`.
2. Crear `ai/internal/ChatClientTextWriter`: la única clase con `ChatClient`, con prompt de sistema y timeout.
3. Escribir un test unitario con `ChatClient` mockeado.

**AC.**
- Test unitario en verde; ninguna clase fuera de `ai.internal` importa `org.springframework.ai` (lo comprueba F4-06).

### `[ ]` F4-03 · notification · **P1**

**Objetivo.** El email de confirmación lo redacta la IA y llega a Mailpit.

**Pasos.**
1. Hacer que `OrderConfirmationListener` llame a `TextWriter`; si falla, usar un texto fijo de respaldo.
2. Hacer que `EmailSender` use `JavaMailSender` por SMTP contra Mailpit.

**AC.**
- Tras `POST /orders`, Mailpit muestra el email con texto generado.
- Con Ollama apagado se envía el texto de respaldo y la publicación del evento se completa.

### `[ ]` F4-04 · ai / config · **P2**

**Objetivo.** Alternativa en la nube por si el modelo local es demasiado lento.

**Pasos.**
1. Crear el perfil `cloud` con un proveedor de Spring AI; la API key se lee de `.env`.

**AC.**
- Arrancando con `dev,cloud` el email se genera con el proveedor externo (lo pruebas tú con tu key).

### `[ ]` F4-05 · testing · **P1**

**Objetivo.** Cada módulo se prueba aislado con `@ApplicationModuleTest` + `Scenario` (Post 9).

**Pasos.**
1. `order`: crear pedido → se publica `OrderCompleted` (`Scenario.stimulate(...).andWaitForEventOfType(...)`).
2. `inventory`: publicar evento → stock descontado (consolidar el test de F2-02).
3. `notification`: con `TextWriter` como `@MockitoBean`, publicar evento → `EmailSender` invocado.

**AC.**
- Los 3 tests en verde sin Ollama ni Redpanda en ejecución.

### `[ ]` F4-06 · modularity · **P1**

**Objetivo.** Resolver la afirmación pendiente del Post 9: ¿detecta `verify()` un `ChatClient` usado fuera de `ai`?

**Pasos.**
1. En una rama temporal, inyectar `ChatClient` directamente en `notification` y ejecutar `ModularityTest`.
2. Documentar el resultado en `docs/verify-chatclient.md`. Hipótesis: `verify()` **no** lo detecta, porque Spring AI es una librería externa y no un módulo de la aplicación.
3. Si no lo detecta, añadir a `ModularityTest` una regla ArchUnit: solo `..ai.internal..` puede depender de `org.springframework.ai..`. Repetir la prueba de la rama.
4. Si el resultado contradice el Post 9, anotar la corrección necesaria del post.

**AC.**
- `docs/verify-chatclient.md` recoge la salida real de ambos casos.
- En la rama temporal `ModularityTest` falla; en `main` pasa.

### `[ ]` F4-07 · docs · **P1**

**Objetivo.** README completo: la guía del proyecto.

**Pasos.**
1. Completar el README con: qué es y qué no es, arquitectura (mermaid del flujo y tabla de módulos), mapa post ↔ código, arranque paso a paso (compose, perfiles, usuarios, URLs: Swagger, Redpanda Console, Mailpit), cómo ejecutar cada tipo de test, tabla de tags, demos (verify roto, caída, ChatClient), troubleshooting (Ollama lento, puertos ocupados) y siguiente paso hacia microservicios.

**AC.**
- Un clon limpio siguiendo solo el README llega al flujo end-to-end completo.

### `[ ]` F4-08 · release · **P1**

**Objetivo.** Cerrar el proyecto con la verificación end-to-end.

**Pasos.**
1. Ejecutar la sección "Verificación end-to-end".
2. Con tu OK: commit y tag `blog-4-ia-testing`.

**AC.**
- Todos los pasos de la verificación end-to-end OK; `git tag` lista los 4 tags.

## Verificación end-to-end

1. `./mvnw clean verify` → unitarios, `ModularityTest`, `@ApplicationModuleTest` y los IT con Testcontainers en verde.
2. `./mvnw test -Pdocs` → diagramas actualizados en `docs/modules/` con los 4 módulos.
3. `docker compose up -d` (con tu OK) → postgres, redpanda, console, ollama y mailpit healthy.
4. Arrancar en `dev`; `curl -u alice:... -X POST /orders` → 201.
5. Comprobar: el stock baja (`GET` o SQL); el email aparece en Mailpit con texto de la IA; el mensaje está en `shop.orders.completed` en la consola; `event_publication` tiene todas las filas completadas; en el log aparecen `alice` y el mismo traceId en la petición y en el listener.
6. Sin credenciales → 401; producto sin stock → 409.
7. Demo de caída (`docs/demo-caida.md`) reproducible.

## Issues abiertas / decisiones pendientes

- **F0-03 · ficheros de configuración** — tu persona prohíbe tocar `application.properties` y `application-prod.properties` sin instrucción explícita. Al ser un proyecto nuevo hay que **crearlos**. Propuesta: autorizarme a crear `application.properties`, `application-dev.properties`, `application-test.properties` y `application-prod.properties` en este repo. Bloquea: F0-03.
- **Testcontainers** — `mvn verify` levantará contenedores (PostgreSQL, Redpanda). `mvn` está autorizado, pero lanzar contenedores por esa vía es un cambio de estado en Docker. Propuesta: autorización permanente para Testcontainers en este proyecto. Bloquea: F2-05, F3-05, F4-08.
- **Repo remoto** — GitHub personal u organización DatIACode: se decide el lunes. No bloquea nada; los tags quedan locales hasta el push.
- **Versiones** — si la última versión de Spring AI no es compatible con la última de Spring Boot, se usa la combinación compatible más reciente. Los nombres de API de los posts se revisan contra esa versión al cerrar cada fase. Bloquea: F0-01.
- **Blogs** — los 4 artículos se escriben al terminar el proyecto, a partir de los blogs antiguos que pasarás. Fuera del alcance de este plan.
