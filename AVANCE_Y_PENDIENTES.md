# OYL Frutos Secos — avance sobre el ZIP del usuario

## Implementado en esta entrega
- Se conserva la navegación y las 15 pantallas originales.
- Room versión 2: migración **1 → 2** sin borrar perfiles, pedidos ni carritos existentes.
- Historial de movimientos de stock local (entradas/salidas), con indicación explícita de pendiente de sincronización.
- Aviso local de stock bajo al modificar existencias.
- Edición de nombre, teléfono y dirección del perfil de cliente en SQLite.
- Contraseñas de nuevas cuentas locales almacenadas mediante PBKDF2 con sal; se admite lectura de credenciales antiguas solo para migración de la demo.
- Se eliminan permisos repetidos del manifest.
- Se añade un backend Spring Boot **independiente**, con H2 y endpoints REST para productos y pedidos. El servidor valida stock y descuenta existencias dentro de una transacción, con bloqueo pesimista de filas.

## Pendiente para cumplir íntegramente la pauta
- Conectar Retrofit de la app al backend, autenticación y autorización de roles reales, sincronización de usuarios e inventario.
- Sustituir la cámara de foto para QR por escaneo continuo (CameraX).
- Integrar mapa embebido con ubicaciones y marcadores reales (requiere configuración de SDK y clave de mapas, si se usa Google Maps).
- Notificaciones push reales con Firebase Cloud Messaging (requiere proyecto Firebase y configuración).
- Sincronización de pedidos, estados y movimientos de stock; no asumir que un pedido local descuenta stock del servidor.
- Pruebas automatizadas, Git/GitHub, Trello y APK release firmado.

## Importante
- El proyecto Android **sigue en modo demostración local**. El backend añadido no se invoca automáticamente.
- La cuenta admin/admin123 es SOLO de demostración. No exponer el backend a internet.
- No se ha ejecutado Android Studio, un emulador ni un build Gradle completo en este entorno.
- Antes de instalar en un equipo con datos existentes, haz una copia de seguridad; se agregó una migración Room 1→2.

## Prueba recomendada
1. Abrir la carpeta OYLFrutosSecos en Android Studio Rabbit 1.
2. Sincronizar Gradle, luego Build > Rebuild Project.
3. Probar registro, login, editar perfil y cerrar/reabrir app para verificar persistencia.
4. Como admin, cambiar inventario y comprobar los movimientos registrados.
5. Abrir backend-springboot por separado en IntelliJ y ejecutar `mvn spring-boot:run` con JDK 17.
