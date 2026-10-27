-- Esquema de Mr Cake derivado de la estructura del respaldo, sin datos.
-- Ejecutar únicamente para crear una base nueva.
CREATE DATABASE mrcake CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mrcake;

CREATE TABLE `cajas` (
  `id_caja` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario_apertura` int(11) NOT NULL,
  `id_usuario_cierre` int(11) DEFAULT NULL,
  `fecha_apertura` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_inicial` decimal(10,2) NOT NULL DEFAULT 0.00,
  `monto_final` decimal(10,2) DEFAULT NULL,
  `total_ingresos` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total_egresos` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` enum('abierta','cerrada') NOT NULL DEFAULT 'abierta',
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_caja`),
  KEY `id_usuario_apertura` (`id_usuario_apertura`),
  KEY `id_usuario_cierre` (`id_usuario_cierre`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `categorias` (
  `id_categoria` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `compras` (
  `id_compra` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `proveedor` varchar(100) DEFAULT NULL,
  `numero_factura` varchar(50) DEFAULT NULL,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `observaciones` varchar(255) DEFAULT NULL,
  `id_proveedor` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_compra`),
  KEY `fk_compras_proveedores` (`id_proveedor`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `configuracion_negocio` (
  `id_configuracion` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_negocio` varchar(100) NOT NULL DEFAULT 'Mr Cake',
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `mensaje_ticket` varchar(255) DEFAULT NULL,
  `moneda` varchar(10) NOT NULL DEFAULT 'USD',
  `ultima_actualizacion` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id_configuracion`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalle_compras` (
  `id_detalle_compra` int(11) NOT NULL AUTO_INCREMENT,
  `id_compra` int(11) NOT NULL,
  `id_ingrediente` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle_compra`),
  KEY `id_compra` (`id_compra`),
  KEY `id_ingrediente` (`id_ingrediente`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalle_produccion` (
  `id_detalle_produccion` int(11) NOT NULL AUTO_INCREMENT,
  `id_produccion` int(11) NOT NULL,
  `id_ingrediente` int(11) NOT NULL,
  `cantidad_utilizada` decimal(10,3) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `costo_unitario` decimal(10,4) NOT NULL DEFAULT 0.0000,
  `costo_total` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_detalle_produccion`),
  KEY `id_produccion` (`id_produccion`),
  KEY `id_ingrediente` (`id_ingrediente`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalle_receta` (
  `id_detalle_receta` int(11) NOT NULL AUTO_INCREMENT,
  `id_receta` int(11) NOT NULL,
  `id_ingrediente` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  PRIMARY KEY (`id_detalle_receta`),
  KEY `id_receta` (`id_receta`),
  KEY `id_ingrediente` (`id_ingrediente`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalle_ventas` (
  `id_detalle_venta` int(11) NOT NULL AUTO_INCREMENT,
  `id_venta` int(11) NOT NULL,
  `id_producto` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `descuento` decimal(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle_venta`),
  KEY `id_venta` (`id_venta`),
  KEY `id_producto` (`id_producto`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ingredientes` (
  `id_ingrediente` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `cantidad_presentacion` decimal(10,3) DEFAULT NULL,
  `precio_compra` decimal(10,2) DEFAULT NULL,
  `stock` decimal(10,3) DEFAULT 0.000,
  `stock_minimo` decimal(10,3) DEFAULT 0.000,
  PRIMARY KEY (`id_ingrediente`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `metodos_pago` (
  `id_metodo_pago` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` enum('activo','inactivo') NOT NULL DEFAULT 'activo',
  PRIMARY KEY (`id_metodo_pago`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `movimientos_caja` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) NOT NULL,
  `id_venta` int(11) DEFAULT NULL,
  `tipo` enum('ingreso','egreso') NOT NULL,
  `concepto` varchar(100) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `observaciones` varchar(255) DEFAULT NULL,
  `id_caja` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_venta` (`id_venta`),
  KEY `fk_movimientos_cajas` (`id_caja`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `movimientos_inventario` (
  `id_movimiento_inventario` int(11) NOT NULL AUTO_INCREMENT,
  `id_ingrediente` int(11) DEFAULT NULL,
  `id_producto` int(11) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo` enum('compra','produccion','venta','ajuste','anulacion') NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `referencia` varchar(100) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `id_compra` int(11) DEFAULT NULL,
  `id_venta` int(11) DEFAULT NULL,
  `id_produccion` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento_inventario`),
  KEY `id_ingrediente` (`id_ingrediente`),
  KEY `id_producto` (`id_producto`),
  KEY `id_usuario` (`id_usuario`),
  KEY `fk_mov_inv_compras` (`id_compra`),
  KEY `fk_mov_inv_ventas` (`id_venta`),
  KEY `fk_mov_inv_producciones` (`id_produccion`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `producciones` (
  `id_produccion` int(11) NOT NULL AUTO_INCREMENT,
  `id_producto` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `cantidad_producida` decimal(10,3) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `costo_total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` enum('pendiente','completada','anulada') NOT NULL DEFAULT 'pendiente',
  `observaciones` varchar(255) DEFAULT NULL,
  `id_receta` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_produccion`),
  KEY `id_producto` (`id_producto`),
  KEY `id_usuario` (`id_usuario`),
  KEY `fk_producciones_recetas` (`id_receta`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `productos` (
  `id_producto` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `precio_venta` decimal(10,2) DEFAULT NULL,
  `stock` decimal(10,3) NOT NULL DEFAULT 0.000,
  `id_categoria` int(11) DEFAULT NULL,
  `codigo_barras` varchar(50) DEFAULT NULL,
  `stock_minimo` decimal(10,3) NOT NULL DEFAULT 0.000,
  PRIMARY KEY (`id_producto`),
  UNIQUE KEY `codigo_barras` (`codigo_barras`),
  KEY `id_categoria` (`id_categoria`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `proveedores` (
  `id_proveedor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `numero_documento` varchar(30) DEFAULT NULL,
  `estado` enum('activo','inactivo') NOT NULL DEFAULT 'activo',
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `recetas` (
  `id_receta` int(11) NOT NULL AUTO_INCREMENT,
  `id_producto` int(11) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `cantidad_producida` decimal(10,3) DEFAULT NULL,
  `unidad_produccion` varchar(20) DEFAULT NULL,
  `instrucciones` text DEFAULT NULL,
  PRIMARY KEY (`id_receta`),
  KEY `id_producto` (`id_producto`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('administrador','empleado') NOT NULL DEFAULT 'empleado',
  `estado` enum('activo','inactivo') NOT NULL DEFAULT 'activo',
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `usuario` (`usuario`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ventas` (
  `id_venta` int(11) NOT NULL AUTO_INCREMENT,
  `numero_ticket` varchar(30) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `id_usuario` int(11) NOT NULL,
  `id_metodo_pago` int(11) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00,
  `descuento` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `monto_recibido` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cambio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` enum('completada','anulada') NOT NULL DEFAULT 'completada',
  `observaciones` varchar(255) DEFAULT NULL,
  `id_caja` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_venta`),
  UNIQUE KEY `numero_ticket` (`numero_ticket`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_metodo_pago` (`id_metodo_pago`),
  KEY `fk_ventas_cajas` (`id_caja`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
