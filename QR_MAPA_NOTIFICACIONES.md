# OYL Frutos Secos — QR, mapa y avisos

## Incorporado
- **QR:** pantalla con lector de QR usando Google Code Scanner (Google Play Services). Lee códigos QR sin tomar una fotografía; el contenido debe ser un ID de producto o terminar en `/ID`. Sigue disponible la consulta manual. En algunos dispositivos el módulo se descarga al primer uso.
- **Mapa:** mapa interactivo OpenStreetMap embebido mediante WebView, con marcador de **Santiago referencial**. Se requiere internet. Se añadió un botón para abrir indicaciones en la aplicación de mapas del teléfono.
- **Notificaciones del dispositivo:** avisos locales generados a partir de nuevos registros de la tabla Room de notificaciones; solicita permiso en Android 13+. Se muestran cuando la app está activa y recibe nuevos registros. El primer arranque no reproduce el historial para evitar una ráfaga de avisos.

## Importante: límites pendientes
- **No es Firebase Cloud Messaging:** las notificaciones push desde el servidor requieren proyecto Firebase, archivo `google-services.json`, token de dispositivo y lógica de envío desde Spring Boot.
- **El mapa usa coordenadas de demostración** y no representa una tienda OYL verificada; reemplazar por dirección/puntos de retiro reales.
- **El backend incluido es independiente:** todavía no se sincronizan pedidos/inventario de Android con Spring Boot. No se debe presentar como integración completa.
- **No se ejecutó un build Android en este entorno.** Si Android Studio muestra un error, copiar el primer error completo de Build.
- **Cuenta admin** `admin` / `admin123` solo para pruebas locales.

## Pruebas
1. Abrir el proyecto en Android Studio y ejecutar `Sync Project with Gradle Files`, `Build > Rebuild Project`.
2. Probar QR en dispositivo con Google Play Services y un código `1` (Almendras).
3. Abrir Mapa con conexión a internet y comprobar el marcador referencial.
4. Conceder notificaciones y crear un pedido como cliente; verificar aviso local. Como administrador, bajar stock bajo 10 para generar aviso.
5. Para probar backend: abrir `backend-springboot` aparte, ejecutar con JDK 17 y Maven. No asumir que Android se comunica con él.
