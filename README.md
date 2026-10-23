# OYL Frutos Secos

Aplicación móvil académica de gestión y venta de frutos secos, desarrollada en **Kotlin**, **Jetpack Compose** y **Material 3**. Incluye persistencia local mediante **Room/SQLite** y un backend de demostración construido con **Java y Spring Boot**.

## Integrante

- Benjamín Ignacio Pérez Pizarro

## Funcionalidades

El proyecto contempla **15 pantallas**: Bienvenida, Registro, Inicio de sesión, Dashboard, Mapa, Catálogo, Detalle de producto, Escáner QR, Carrito, Confirmación de pedido, Gestión de productos, Inventario, Pedidos, Notificaciones y Perfil.

- **Usuarios:** registro, inicio de sesión y perfil con datos locales.
- **Catálogo y carrito:** consulta de productos, cantidades y preparación de pedidos.
- **Inventario:** administración de existencias y movimientos de stock.
- **Persistencia:** almacenamiento de información local en Room/SQLite.
- **Códigos QR:** lectura mediante Google Code Scanner, con consulta de productos.
- **Mapa:** visualización referencial mediante OpenStreetMap en WebView.
- **Notificaciones:** avisos locales en el dispositivo.
- **Modo conectado opcional:** integración con una API Spring Boot para catálogo, pedidos y ajustes de stock, según la configuración de la aplicación.

## Tecnologías

| Área | Herramientas |
| --- | --- |
| Aplicación Android | Kotlin, Android Studio, Jetpack Compose, Material 3 |
| Almacenamiento local | Room / SQLite |
| Comunicación HTTP | Retrofit |
| Lectura QR | Google Code Scanner |
| Mapa | OpenStreetMap, WebView |
| Backend | Java 17, Spring Boot 3.3.5, Spring Data JPA |
| Base de datos del backend | H2 |
| Construcción | Gradle para Android y Maven para backend |

## Estructura del repositorio

```text
OYLFrutosSecos/
├── app/                    # Código fuente y recursos de Android
├── backend-springboot/     # API REST en Java
├── gradle/                 # Wrapper y catálogo de versiones
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## Requisitos

- Android Studio y Android SDK Platform 35.
- **JDK 17** para el proyecto.
- Teléfono Android o emulador para probar la aplicación.
- Para el backend: Java 17 y Maven.
- Conexión a internet para descargar las dependencias iniciales.

## Cómo ejecutar Android

1. Clona el repositorio o descárgalo desde **Code → Download ZIP**.
2. Abre la **carpeta raíz** `OYLFrutosSecos` en Android Studio.
3. Configura JDK 17 y sincroniza el proyecto con Gradle.
4. Selecciona el módulo `app` y un teléfono o emulador.
5. Pulsa **Run ▶**.

También puedes intentar compilar un APK de depuración desde la carpeta raíz:

```bat
gradlew.bat assembleDebug
```

Si la compilación finaliza correctamente, el APK suele quedar en `app/build/outputs/apk/debug/app-debug.apk`.

## Cómo ejecutar Spring Boot

Desde la carpeta `backend-springboot`:

```bash
mvn spring-boot:run
```

El servidor utiliza el **puerto 8080**. Desde el PC, prueba:

```text
http://localhost:8080/api/products
```

El backend utiliza una base H2 local. No es necesario tener MySQL instalado para esta configuración.

### Conectar el teléfono con el backend

1. Inicia Spring Boot en el computador.
2. Conecta el computador y el teléfono a la **misma red Wi-Fi**.
3. En Windows, ejecuta `ipconfig` y anota la dirección IPv4 del computador.
4. En el navegador del teléfono, prueba `http://IP_DEL_PC:8080/api/products`.
5. En la aplicación, abre **Perfil**, escribe `http://IP_DEL_PC:8080/`, activa el modo servidor, guarda la configuración y utiliza **Probar conexión**.

En un emulador Android, normalmente se utiliza `http://10.0.2.2:8080/` para acceder al servidor del computador. En un teléfono físico no uses `localhost` como dirección del PC.

Consulta las instrucciones ampliadas en [CONEXION_ANDROID_SPRING_BOOT.md](CONEXION_ANDROID_SPRING_BOOT.md).

## Alcance y limitaciones

Este repositorio corresponde a un **proyecto académico en desarrollo**. Las funciones deben validarse en Android Studio y en un dispositivo real antes de considerarse completamente verificadas.

- Los datos del modo local se almacenan en Room/SQLite; el backend mantiene sus propios datos en H2.
- Los pedidos creados en modo local **no se sincronizan automáticamente** con el servidor.
- El mapa presenta una ubicación de demostración, no una tienda real verificada.
- Las notificaciones son **locales**; no hay integración con Firebase Cloud Messaging.
- El backend de demostración no cuenta con un sistema completo de autenticación y autorización. Úsalo únicamente en una red privada de confianza.
- No se implementan pagos reales ni se debe usar información personal sensible para las pruebas.

## Documentación adicional

- [LEEME_OYL.md](LEEME_OYL.md): pantallas y funcionamiento local.
- [CONEXION_ANDROID_SPRING_BOOT.md](CONEXION_ANDROID_SPRING_BOOT.md): conexión entre Android y Spring Boot.
- [QR_MAPA_NOTIFICACIONES.md](QR_MAPA_NOTIFICACIONES.md): QR, mapa y avisos.
- [AVANCE_Y_PENDIENTES.md](AVANCE_Y_PENDIENTES.md): avances anteriores y tareas pendientes.

---

**OYL Frutos Secos — Proyecto académico Android y Spring Boot.**
