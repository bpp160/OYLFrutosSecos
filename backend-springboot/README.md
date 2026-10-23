# Backend Spring Boot OYL — conexión Android

Requiere JDK 17 y Maven. Desde esta carpeta ejecutar `mvn spring-boot:run`. Servidor en puerto 8080. Base H2 persistente en `./data/oyl`.

Endpoints: `GET /api/products`, `GET /api/products/{id}`, `POST /api/products`, `GET /api/orders`, `POST /api/orders`, `PATCH /api/orders/{id}/status`, `PATCH /api/products/{id}/stock`.

Ejemplo pedido: `{"clientId":1,"delivery":"Retiro en tienda","items":[{"productId":1,"quantity":2}]}`.

La confirmación del pedido valida existencias y precios desde la base del servidor, usando bloqueo transaccional. Los productos de muestra se crean al iniciar la base vacía. **Sin autenticación ni autorización**: solo para demostración en red local de confianza, no exponer a internet. Consultar `../CONEXION_ANDROID_SPRING_BOOT.md` para la configuración del teléfono.
