# OYL Frutos Secos — Android conectado a Spring Boot

## Qué quedó implementado
- Configuración de URL del servidor en **Perfil**, interruptor para activar o desactivar el modo conectado, botón **Probar conexión**.
- Descarga de catálogo desde `GET /api/products` a Room (se conservan descripción, fotos y gramaje locales para IDs existentes).
- Alta/edición de productos desde la pantalla administrativa a `POST /api/products` cuando está activado el servidor.
- Confirmación de pedidos a `POST /api/orders`: **solo se guarda localmente después de que el servidor confirma**. El servidor calcula total y descuenta existencias bajo transacción.
- Ajustes de inventario a `PATCH /api/products/{id}/stock` y registro local del movimiento confirmado.
- Consulta manual de pedidos del servidor desde Perfil (`GET /api/orders`), con identificación remota persistida en Room.
- Cambio de estado de pedidos vinculados a `PATCH /api/orders/{id}/status`.
- Migración Room 2→3 que agrega `serverId` sin borrar los pedidos existentes. Se conservan migraciones anteriores.
- Modo local anterior disponible cuando el interruptor está apagado. **No hay sincronización automática de pedidos o movimientos creados en modo local.**

## Cómo ejecutarlo en tu PC y teléfono
1. Descomprime el ZIP. Abre la carpeta `OYLFrutosSecos/backend-springboot` en IntelliJ o terminal. Requiere JDK 17 y Maven. Ejecuta `mvn spring-boot:run`. El servidor queda en **puerto 8080** y almacena datos en H2 dentro de `backend-springboot/data/`.
2. En Windows, abre **CMD** y ejecuta `ipconfig`. Copia la **Dirección IPv4** del adaptador Wi-Fi, por ejemplo `192.168.1.20`.
3. Conecta el teléfono y PC al **mismo Wi-Fi**. Comprueba en el navegador del teléfono `http://IP_DEL_PC:8080/api/products` (ej. `http://192.168.1.20:8080/api/products`). Si no abre, revisa Firewall de Windows (permite Java en redes privadas) y que el servidor esté iniciado.
4. Abre la aplicación Android en el teléfono, entra en **Perfil** y escribe `http://IP_DEL_PC:8080/` (sustituye IP_DEL_PC por la IPv4 real). Activa **Usar servidor**, pulsa **Guardar conexión**, luego **Probar conexión** y **Sincronizar catálogo**.
5. Registra un pedido y comprueba `http://IP_DEL_PC:8080/api/orders` en el navegador. Para administrar, entra como admin y pulsa **Consultar pedidos del servidor** desde Perfil.
6. En emulador Android, la URL habitual para alcanzar el PC es `http://10.0.2.2:8080/`. En teléfono físico **NO** uses `localhost` ni `10.0.2.2`.
7. Si no quieres usar servidor, apaga el interruptor en Perfil y guarda: vuelve al modo local.

## Advertencias importantes
- **Proyecto académico de demostración:** backend sin autenticación ni permisos por rol. Un tercero en la red podría acceder a los endpoints. Usar solo en Wi-Fi privado de confianza; no publicar en internet ni usar datos reales sensibles.
- `usesCleartextTraffic=true` permite HTTP en la red local para pruebas. Para producción usar HTTPS y autenticación.
- Las cuentas siguen siendo locales; el backend recibe `clientId` local, que **no es globalmente único** entre distintos teléfonos. Para múltiples dispositivos se requiere autenticación y cuentas centralizadas.
- No hay sincronización automática de cambios offline ni de notificaciones push Firebase.
- Los pedidos anteriores que solo estaban en Room **no se suben al servidor**; los pedidos nuevos con modo conectado sí se envían.
- Los pedidos enviados al servidor y confirmados se guardan localmente con ID remoto; en caso de cierre de la app entre la respuesta del servidor y el guardado local podría necesitarse reconciliación manual.
- El servidor inicia con 6 productos solo si la base H2 está vacía; si ya se usó una base anterior con 3, añade los demás desde la gestión de productos.
- No se pudo compilar ni ejecutar Android/Gradle/Maven en este entorno: se debe validar en Android Studio y en el teléfono antes de considerarlo terminado.
