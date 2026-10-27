-- Datos ficticios para una base mrcake recién creada.
-- Antes de ejecutar este archivo, crear el administrador inicial con ID 1.
USE mrcake;

INSERT INTO categorias (id_categoria, nombre, descripcion) VALUES
    (1, 'Pastelería demo', 'Categoría de demostración'),
    (2, 'Bebidas demo', 'Categoría de demostración');

INSERT INTO productos
    (nombre, descripcion, precio_venta, stock, id_categoria, codigo_barras, stock_minimo)
VALUES
    ('Porción de pastel demo', 'Producto ficticio', 2.50, 20.000, 1, 'DEMO-0001', 3.000),
    ('Bebida demo', 'Producto ficticio', 1.25, 15.000, 2, 'DEMO-0002', 3.000);

-- FrmVentas utiliza actualmente el método de pago con ID 1.
INSERT INTO metodos_pago (id_metodo_pago, nombre, descripcion, estado)
VALUES (1, 'Efectivo', 'Método de demostración', 'activo');

-- FrmVentas utiliza actualmente el usuario con ID 1.
-- Si no existe un administrador activo con ese ID, esta sentencia no inserta una caja.
INSERT INTO cajas (id_usuario_apertura, monto_inicial, estado, observaciones)
SELECT id_usuario, 0.00, 'abierta', 'Caja de demostración'
FROM usuarios WHERE id_usuario = 1 AND rol = 'administrador' AND estado = 'activo';
