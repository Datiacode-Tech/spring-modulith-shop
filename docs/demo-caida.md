# Demo: matar la app a mitad de un evento

Qué demuestra: con el **Event Publication Registry** de Spring Modulith, un evento cuyo listener no terminó porque la aplicación murió no se pierde. Al reiniciar, se reprocesa **solo el listener pendiente**; los que ya habían terminado no se repiten.

## Cómo funciona

1. `OrderService.create` guarda el pedido y publica `OrderCompleted` **en la misma transacción**. Modulith añade a la tabla `event_publication` una fila por cada listener interesado.
2. Tras el commit, cada `@ApplicationModuleListener` se ejecuta en su propio hilo y en su propia transacción. Al terminar bien, su fila pasa a `COMPLETED`.
3. Si la app muere antes, la fila se queda incompleta. Con `spring.modulith.events.republish-outstanding-events-on-restart=true`, el siguiente arranque la vuelve a enviar.

El perfil `demo` añade una pausa de 20 s al listener de `notification` (`shop.demo.notification-delay`) para que dé tiempo a matar la app mientras procesa el evento.

## Pasos

```bash
# 0. Infraestructura levantada y app con los perfiles dev + demo
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,demo

# 1. Otro terminal: crear un pedido
curl -u alice:change-me -X POST localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"items":[{"sku":"MN-001","quantity":1}]}'

# 2. Antes de que pasen 20 s: matar la app sin darle opción a cerrar limpio
pkill -9 -f '[S]hopApplication'   # los corchetes evitan que pkill se encuentre a sí mismo en un script

# 3. Ver el registro de publicaciones
psql -h localhost -p 5442 -U shop -d shop -c "
  SELECT substring(listener_id from '([A-Za-z]+Listener)') AS listener, status,
         completion_date IS NOT NULL AS completed
  FROM event_publication ORDER BY publication_date DESC, listener_id LIMIT 3;"

# 4. Reiniciar la app (con dev basta; con dev,demo el reproceso vuelve a esperar 20 s) y repetir la consulta del paso 3
```

## Resultado real (2026-10-19, fase 4)

Cada pedido genera tres publicaciones: `inventory`, `notification` y la externalización a Kafka (`EventExternalizerModuleListener`).

Tras el `kill -9`:

```
            listener             |   status   | completed 
---------------------------------+------------+-----------
 EventExternalizerModuleListener | COMPLETED  | t
 OrderConfirmationListener       | PROCESSING | f
 OrderCompletedListener          | COMPLETED  | t
```

`inventory` ya había descontado el stock y el evento ya estaba en Kafka; `notification` estaba en mitad de su pausa.

Tras reiniciar:

```
            listener             |  status   | completed 
---------------------------------+-----------+-----------
 EventExternalizerModuleListener | COMPLETED | t
 OrderConfirmationListener       | COMPLETED | t
 OrderCompletedListener          | COMPLETED | t
```

```
INFO [] ... [shop-async-1] c.d.s.n.i.OrderConfirmationListener : Order 14 confirmation requested by user <none: no SecurityContext on this thread>
INFO [] ... [shop-async-1] c.d.s.notification.internal.EmailSender : Email sent to alice@example.com | Order #14 confirmed
```

Solo se reprocesó el listener de `notification`: ni el stock se descontó dos veces ni el evento se publicó otra vez en Kafka. El usuario y el traceId salen vacíos porque el reproceso no viene de ninguna petición HTTP.

## Matiz importante

Que se reprocese implica **entrega al menos una vez** (*at-least-once*). Si la app muere *después* de que el listener haga su trabajo pero *antes* de marcar la fila como completada, el listener se ejecutará otra vez. Los listeners tienen que ser idempotentes, o tolerar algún duplicado como este email.

La versión automática de esta demo es `EventRepublicationTests`: arranca dos contextos de Spring seguidos contra el mismo PostgreSQL (Testcontainers), con un listener que falla en el primero.
