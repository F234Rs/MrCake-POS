# Preparación de la base de demostración

Los scripts fueron comparados con el respaldo, pero aún no se han probado en una instalación nueva de MariaDB.

Estos pasos son para una instalación nueva y vacía. No ejecutes los scripts sobre la base que contiene datos del negocio. `mrcake_schema.sql` crea la base `mrcake` y fallará si ya existe una con ese nombre; elegí una instancia de MariaDB separada para la demostración si ya tenés una base con ese nombre. El proyecto debe apuntar a esa instancia para usarla.

1. Con un cliente MariaDB y un usuario con permisos adecuados, ejecutá `database/mrcake_schema.sql`. El archivo crea las 18 tablas sin datos del respaldo.
2. Creá el administrador inicial con la herramienta interactiva indicada abajo.
3. Ejecutá `database/datos_demo.sql` una sola vez. Carga productos ficticios, categorías, el método de pago 1 y una caja abierta para el administrador 1. La inserción de la caja no crea ninguna fila si falta ese administrador; verificá que exista una caja abierta antes de vender.
4. Revisá la conexión local en `Conexion.java` y ejecutá `FrmLogin` desde NetBeans.

El esquema conserva MyISAM como el respaldo de referencia. Ese motor no garantiza transacciones ni claves foráneas: `rollback` no revierte las operaciones de ventas y las comprobaciones de stock y precio no son atómicas ante cambios concurrentes. Para uso real debe evaluarse una migración independiente a un motor transaccional.

## Crear el administrador sin publicar una contraseña

La herramienta `tools/CrearAdministrador.java` pide los datos en una terminal interactiva. Las contraseñas se escriben sin mostrarse y la del administrador se almacena como hash BCrypt; no se pasa ninguna contraseña como argumento ni se escribe en archivos del proyecto. La herramienta exige que la tabla `usuarios` esté vacía y crea el administrador con ID 1, porque `FrmVentas` usa actualmente ese ID fijo.

Primero descargá las dependencias del proyecto con Maven. En PowerShell, desde la raíz del proyecto y con JDK 26 activo:

```powershell
$jbcrypt = Join-Path $env:USERPROFILE '.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar'
$mariadb = Join-Path $env:USERPROFILE '.m2\repository\org\mariadb\jdbc\mariadb-java-client\3.5.3\mariadb-java-client-3.5.3.jar'
java -cp "$jbcrypt;$mariadb" tools/CrearAdministrador.java
```

Si Maven usa un repositorio local distinto, ajustá esas rutas. Ingresá la URL y las credenciales de la instancia de demostración, el nombre y usuario deseados, y una contraseña nueva de al menos 12 caracteres y hasta 72 bytes UTF-8. No ingreses contraseñas en el historial de comandos. Después, iniciá sesión con el usuario y la contraseña elegidos.
