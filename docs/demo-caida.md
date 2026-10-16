# Demo: matar la app a mitad de un evento

Qué demuestra: con el **Event Publication Registry** de Spring Modulith, un evento cuyo listener no terminó porque la aplicación murió no se pierde. Al reiniciar, se reprocesa **solo el listener pendiente**; los que ya habían terminado no se repiten.

## Cómo funciona

1. `OrderService.create` guarda el pedido y publica `OrderCompleted` **en la misma transacción**. Modulith añade a la tabla `event_publication` una fila por cada listener interesado.
2. Tras el commit, cada `@ApplicationModuleListener` se ejecuta en su propio hilo y en su propia transacción. Al terminar bien, su fila pasa a `COMPLETED`.
3. Si la app muere antes, la fila se queda incompleta. Con `spring.modulith.events.republish-outstanding-events-on-restart=true`, el siguiente arranque la vuelve a enviar.

El perfil `demo` añade una pausa de 20 s al listener de `notification` (`shop.demo.notification-delay`) para que dé tiempo a matar la app mientras procesa el evento.

## Pasos

```bash
# 0. Base de datos levantada y app con los perfiles dev + demo
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,demo

# 1. Otro terminal: crear un pedido
curl -X POST localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"customer":"alice","items":[{"sku":"MN-001","quantity":1}]}'

# 2. Antes de que pasen 20 s: matar la app sin darle opción a cerrar limpio
pkill -9 -f ShopApplication

# 3. Ver el registro de publicaciones
psql -h localhost -p 5442 -U shop -d shop -c "
  SELECT substring(listener_id from '([A-Za-z]+Listener)') AS listener, status,
         completion_date IS NOT NULL AS completed
  FROM event_publication ORDER BY publication_date DESC LIMIT 2;"

# 4. Reiniciar la app (mismo comando del paso 0), esperar unos 20 s y repetir la consulta del paso 3
```

## Resultado real (2026-10-16)

Tras el `kill -9`:

```
         listener          |   status   | completed
---------------------------+------------+-----------
 OrderCompletedListener    | COMPLETED  | t
 OrderConfirmationListener | PROCESSING | f
```

`inventory` ya había descontado el stock; `notification` estaba en mitad de su pausa.

Tras reiniciar:

```
         listener          |  status   | completed
---------------------------+-----------+-----------
 OrderCompletedListener    | COMPLETED | t
 OrderConfirmationListener | COMPLETED | t
```

```
INFO ... [shop-async-1] c.d.s.notification.internal.EmailSender : Email to alice@example.com | Order #3 confirmed | ...
```

Solo se reprocesó el listener de `notification`: el stock no se descontó dos veces.

## Matiz importante

Que se reprocese implica **entrega al menos una vez** (*at-least-once*). Si la app muere *después* de que el listener haga su trabajo pero *antes* de marcar la fila como completada, el listener se ejecutará otra vez. Los listeners tienen que ser idempotentes, o tolerar algún duplicado como este email.

La versión automática de esta demo es `EventRepublicationTests`: arranca dos contextos de Spring seguidos contra el mismo PostgreSQL (Testcontainers), con un listener que falla en el primero.
