# MrCake POS

Sistema de punto de venta e inventario para Mr Cake. La aplicación de escritorio está desarrollada con Java Swing y utiliza MariaDB para almacenar la información del negocio. Incluye un servicio complementario de escaneo de códigos de barras.

## Tecnologías

- Java y Swing, con formularios editables en Apache NetBeans.
- Maven, con MariaDB Connector/J y BCrypt.
- MariaDB.
- Node.js, Express y ZXing Browser para el escáner.

## Componentes

- **Inicio de sesión:** validación de usuarios y contraseñas mediante BCrypt.
- **Dashboard:** resumen de ventas, stock bajo y últimas ventas.
- **Productos:** consulta y mantenimiento de productos y existencias.
- **Ventas:** carrito por código de barras, cobro y emisión de tickets.
- **MrCakeScanner:** página para leer códigos con una cámara y enviarlos a la aplicación de ventas.

## Preparación local

1. Abrí la carpeta del proyecto en Apache NetBeans con un JDK compatible con la versión configurada en `pom.xml` (actualmente Java 26) y Maven.
2. Prepará una base de datos MariaDB con el esquema requerido por la aplicación. Los respaldos con datos reales no forman parte del repositorio.
3. Revisá la configuración local de conexión en `src/main/java/Conexion.java`. Las credenciales reales no deben incluirse en un commit.
4. Ejecutá el proyecto desde NetBeans; la acción de ejecución inicia `FrmLogin`.

Para iniciar el servicio de escaneo, desde `MrCakeScanner` instalá las dependencias con `npm ci` y ejecutá `node server.js`. El servicio escucha en el puerto 8080; la aplicación de Ventas consulta el código recibido en el equipo local.

El servicio del escáner escucha en todas las interfaces de red y no implementa autenticación. Usalo únicamente en una red de confianza mientras no se agreguen controles de acceso.

## Base de datos para demostración

Los scripts de `database/` están preparados para crear una base nueva y cargar datos ficticios. Se compararon con la estructura del respaldo, pero **todavía no se han probado en una instalación nueva de MariaDB**. Seguí el orden y las instrucciones de [preparación de la base](docs/preparacion_base.md), incluida la creación interactiva del administrador. No ejecutes los scripts de demostración sobre la base de trabajo existente.

La carpeta `docs/` contiene las instrucciones para preparar la base de demostración.
