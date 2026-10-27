public class FrmVentas extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmVentas.class.getName());

private final java.text.DecimalFormat formato =
        new java.text.DecimalFormat("0.00");

private final javax.swing.table.DefaultTableModel modelo =
        new javax.swing.table.DefaultTableModel(
            new Object[]{"Código", "Producto", "Precio", "Cantidad", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
private java.util.concurrent.ScheduledExecutorService receptorEscaner;
private final FrmDashboard dashboard;
private boolean volviendoAlInicio;
private String ultimoAvisoCarrito;

public FrmVentas() {
    this(null);
}

public FrmVentas(FrmDashboard dashboard) {
    this.dashboard = dashboard;
    initComponents();
    btnVolverInicio.addActionListener(e -> volverAlInicio());
    addWindowFocusListener(new java.awt.event.WindowAdapter() {
        @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
            if (!(e.getOppositeWindow() instanceof javax.swing.JDialog)) {
                verificarCarritoAlVolver();
            }
        }
    });
    addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            if (receptorEscaner != null) {
                receptorEscaner.shutdownNow();
            }
        }
    });
    
    txtMontoRecibido.setMinimumSize(new java.awt.Dimension(120, 25));
txtMontoRecibido.setPreferredSize(new java.awt.Dimension(120, 25));
txtMontoRecibido.setSize(120, 25);

    tablaCarrito.setModel(modelo);
    lblTotal.setText("Total: $0.00");


    txtCodigoBarras.addActionListener(e -> agregarProducto());
    txtMontoRecibido.addActionListener(e -> calcularCambio());
    btnFinalizarVenta.addActionListener(e -> finalizarVenta());
    txtMontoRecibido.addFocusListener(new java.awt.event.FocusAdapter() {
    @Override
    public void focusGained(java.awt.event.FocusEvent e) {
        txtMontoRecibido.selectAll();
    }
});
    iniciarRecepcionEscaner();
}

private void volverAlInicio() {
    if (dashboard != null) {
        dashboard.mostrarDashboard();
        return;
    }
    if (volviendoAlInicio) {
        return;
    }
    volviendoAlInicio = true;
    if (dashboard == null) {
        FrmDashboard inicio = new FrmDashboard();
        inicio.setLocationRelativeTo(null);
        inicio.setVisible(true);
    }
    dispose();
}

private void iniciarRecepcionEscaner() {
    receptorEscaner =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

    receptorEscaner.scheduleWithFixedDelay(() -> {
        java.net.HttpURLConnection conexion = null;

        try {
            java.net.URL url =
                new java.net.URL("http://127.0.0.1:8080/codigo");

            conexion = (java.net.HttpURLConnection)
                url.openConnection();

            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(300);
            conexion.setReadTimeout(300);

            String codigo;

            try (java.io.InputStream entrada =
                    conexion.getInputStream()) {

                codigo = new String(
                    entrada.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
                ).trim();
            }

            if (!codigo.isEmpty()) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    txtCodigoBarras.setText(codigo);
                    agregarProducto();
                });
            }

        } catch (java.io.IOException ex) {
            // El escáner puede estar desconectado temporalmente.
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }, 0, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
}

private void agregarProducto() {
    String codigo = txtCodigoBarras.getText().trim();

    if (codigo.isEmpty()) {
        return;
    }

    String sql = """
        SELECT codigo_barras, nombre, precio_venta
        FROM productos
        WHERE codigo_barras = ?
        """;

    try (java.sql.Connection con = Conexion.conectar();
         java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, codigo);

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                javax.swing.JOptionPane.showMessageDialog(
                    this, "No se encontró el producto: " + codigo
                );
                return;
            }

            String nombre = rs.getString("nombre");
            java.math.BigDecimal precio =
                    rs.getBigDecimal("precio_venta");

            if (precio == null) {
                javax.swing.JOptionPane.showMessageDialog(
                    this, "El producto no tiene precio."
                );
                return;
            }

            // Si ya está en el carrito, aumenta su cantidad.
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (codigo.equals(modelo.getValueAt(i, 0).toString())) {
                    java.math.BigDecimal precioCarrito = new java.math.BigDecimal(
                            modelo.getValueAt(i, 2).toString().replace(",", "."));
                    if (precio.compareTo(precioCarrito) != 0) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "El precio de " + nombre + " cambió. Quitá el producto del carrito y agregalo de nuevo.");
                        return;
                    }
                    int cantidad =
                            Integer.parseInt(modelo.getValueAt(i, 3).toString()) + 1;

                    modelo.setValueAt(cantidad, i, 3);
                    actualizarSubtotal(i, precio);
                    actualizarTotal();
                    
                    return;
                }
            }

            modelo.addRow(new Object[]{
                codigo, nombre, formato.format(precio),
                1, formato.format(precio)
            });

            actualizarTotal();
        }

    } catch (java.sql.SQLException ex) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Error al buscar producto: " + ex.getMessage()
        );
    } finally {
        txtCodigoBarras.setText("");
        txtCodigoBarras.requestFocusInWindow();
    }
    
}

private String validarCarrito(java.sql.Connection con) throws java.sql.SQLException {
    String sql = "SELECT nombre, precio_venta, stock FROM productos WHERE codigo_barras = ?";
    try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            String codigo = modelo.getValueAt(i, 0).toString();
            ps.setString(1, codigo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "El producto " + codigo + " ya no existe. Quitalo del carrito.";
                java.math.BigDecimal precio = rs.getBigDecimal("precio_venta");
                java.math.BigDecimal precioCarrito = new java.math.BigDecimal(
                        modelo.getValueAt(i, 2).toString().replace(",", "."));
                if (precio == null || precio.compareTo(precioCarrito) != 0) {
                    return "El precio de " + rs.getString("nombre")
                            + " cambió. Quitá el producto del carrito y agregalo de nuevo.";
                }
                java.math.BigDecimal stock = rs.getBigDecimal("stock");
                java.math.BigDecimal cantidad = new java.math.BigDecimal(modelo.getValueAt(i, 3).toString());
                if (stock == null || stock.compareTo(cantidad) < 0) {
                    return "Stock insuficiente para " + rs.getString("nombre")
                            + ". Ajustá el carrito antes de cobrar.";
                }
            }
        }
    }
    return null;
}

private void verificarCarritoAlVolver() {
    if (modelo.getRowCount() == 0) {
        ultimoAvisoCarrito = null;
        return;
    }
    String aviso;
    try (java.sql.Connection con = Conexion.conectar()) {
        aviso = validarCarrito(con);
    } catch (java.sql.SQLException ex) {
        aviso = "No se pudo verificar el carrito: " + ex.getMessage();
    }
    if (aviso != null && !aviso.equals(ultimoAvisoCarrito)) {
        ultimoAvisoCarrito = aviso;
        javax.swing.JOptionPane.showMessageDialog(this, aviso);
    } else if (aviso == null) {
        ultimoAvisoCarrito = null;
    }
}
private void calcularCambio() {
    try {
        String texto = txtMontoRecibido.getText().trim()
                .replace(",", ".");

        if (texto.isEmpty()) {
            lblCambio.setText("Cambio: $0.00");
            return;
        }

        java.math.BigDecimal recibido =
                new java.math.BigDecimal(texto);

        String textoTotal = lblTotal.getText()
                .replace("Total: $", "").trim();

        java.math.BigDecimal total =
                new java.math.BigDecimal(textoTotal);

        if (recibido.compareTo(java.math.BigDecimal.ZERO) < 0) {
            lblCambio.setText("Monto inválido");
            return;
        }

        if (recibido.compareTo(total) < 0) {
            lblCambio.setText("Falta: $"
                    + formato.format(total.subtract(recibido)));
            return;
        }

        lblCambio.setText("Cambio: $"
                + formato.format(recibido.subtract(total)));

    } catch (NumberFormatException ex) {
        lblCambio.setText("Monto inválido");
    }
}

private void actualizarSubtotal(
        int fila, java.math.BigDecimal precio) {

    int cantidad = Integer.parseInt(
            modelo.getValueAt(fila, 3).toString()
    );

    java.math.BigDecimal subtotal = precio.multiply(
            java.math.BigDecimal.valueOf(cantidad)
    );

    modelo.setValueAt(formato.format(subtotal), fila, 4);
}

private void actualizarTotal() {
    java.math.BigDecimal total = java.math.BigDecimal.ZERO;

    for (int i = 0; i < modelo.getRowCount(); i++) {
        String valor = modelo.getValueAt(i, 4).toString();

        total = total.add(new java.math.BigDecimal(valor));
    }

    lblTotal.setText("Total: $" + formato.format(total));
    calcularCambio();
}

private void quitarProducto() {
    int fila = tablaCarrito.getSelectedRow();

    if (fila < 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Seleccioná un producto del carrito."
        );
        return;
    }

    modelo.removeRow(fila);
    actualizarTotal();
}

private void limpiarCarrito() {
    modelo.setRowCount(0);
    actualizarTotal();
}
private void probarImpresora() {
    javax.print.PrintService[] impresoras =
            javax.print.PrintServiceLookup.lookupPrintServices(
                    null, null
            );

    for (javax.print.PrintService impresora : impresoras) {
        System.out.println(
                "Impresora detectada: " + impresora.getName()
        );
    }

    javax.swing.JOptionPane.showMessageDialog(
            this,
            "Revisá la consola Output de NetBeans."
    );
}


private void imprimirTicket(String contenido) {
    try {
        javax.print.PrintService impresoraElegida = null;

        javax.print.PrintService[] impresoras =
                javax.print.PrintServiceLookup.lookupPrintServices(
                        null, null
                );

        for (javax.print.PrintService impresora : impresoras) {
            if (impresora.getName().equalsIgnoreCase("POS58 Printer")) {
                impresoraElegida = impresora;
                break;
            }
        }

        if (impresoraElegida == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "No se encontró la impresora POS58 Printer."
            );
            return;
        }

        byte[] bytes = contenido.getBytes(
                java.nio.charset.Charset.forName("CP437")
        );

        javax.print.Doc documento = new javax.print.SimpleDoc(
                bytes,
                javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE,
                null
        );

        javax.print.DocPrintJob trabajo =
                impresoraElegida.createPrintJob();

        trabajo.print(documento, null);

    } catch (javax.print.PrintException e) {
        javax.swing.JOptionPane.showMessageDialog(
                this, "La venta se guardó, pero ocurrió un error al imprimir:\n"
                        + e.getMessage()
        );
    }
}


private void finalizarVenta() {

    if (modelo.getRowCount() == 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "El carrito está vacío."
        );
        return;
    }

    java.math.BigDecimal total;
    java.math.BigDecimal recibido;

    try {
        total = new java.math.BigDecimal(
            lblTotal.getText()
                .replace("Total: $", "")
                .trim()
                .replace(",", ".")
        );

        recibido = new java.math.BigDecimal(
            txtMontoRecibido.getText()
                .trim()
                .replace(",", ".")
        );

    } catch (NumberFormatException ex) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Ingresá un monto recibido válido."
        );
        return;
    }

    if (recibido.compareTo(total) < 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "El dinero recibido no alcanza para pagar."
        );
        return;
    }

    if (recibido.compareTo(java.math.BigDecimal.ZERO) < 0
            || total.compareTo(java.math.BigDecimal.ZERO) <= 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "El monto o el total no son válidos."
        );
        return;
    }

    java.math.BigDecimal cambio = recibido.subtract(total);

    String numeroTicket = "MC-" +
        new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS")
            .format(new java.util.Date());

    java.sql.Connection con = null;

    try {
        con = Conexion.conectar();
        String avisoCarrito = validarCarrito(con);
        if (avisoCarrito != null) {
            javax.swing.JOptionPane.showMessageDialog(this, avisoCarrito);
            return;
        }
        con.setAutoCommit(false);

        // 1. Buscar y bloquear una caja abierta.
        int idCaja;

        String sqlCaja = """
            SELECT id_caja
            FROM cajas
            WHERE estado = 'abierta'
            ORDER BY id_caja
            LIMIT 1
            FOR UPDATE
            """;

        try (java.sql.PreparedStatement ps =
                    con.prepareStatement(sqlCaja);
             java.sql.ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new java.sql.SQLException(
                    "No hay una caja abierta."
                );
            }

            idCaja = rs.getInt("id_caja");
        }

        // 2. Registrar la venta.
        String sqlVenta = """
            INSERT INTO ventas
            (numero_ticket, id_usuario, id_metodo_pago,
             subtotal, descuento, total, monto_recibido,
             cambio, estado, id_caja)
            VALUES (?, 1, 1, ?, 0.00, ?, ?, ?,
                    'completada', ?)
            """;

        int idVenta;

        try (java.sql.PreparedStatement ps =
                con.prepareStatement(
                    sqlVenta,
                    java.sql.Statement.RETURN_GENERATED_KEYS
                )) {

            ps.setString(1, numeroTicket);
            ps.setBigDecimal(2, total);
            ps.setBigDecimal(3, total);
            ps.setBigDecimal(4, recibido);
            ps.setBigDecimal(5, cambio);
            ps.setInt(6, idCaja);

            ps.executeUpdate();

            try (java.sql.ResultSet rs =
                    ps.getGeneratedKeys()) {

                if (!rs.next()) {
                    throw new java.sql.SQLException(
                        "No se obtuvo el ID de la venta."
                    );
                }

                idVenta = rs.getInt(1);
            }
        }

        // 3. Guardar cada producto y descontar existencias.
        String sqlBuscarProducto = """
            SELECT id_producto, stock
            FROM productos
            WHERE codigo_barras = ?
            FOR UPDATE
            """;

        String sqlDetalle = """
            INSERT INTO detalle_ventas
            (id_venta, id_producto, cantidad,
             precio_unitario, descuento, subtotal)
            VALUES (?, ?, ?, ?, 0.00, ?)
            """;

        String sqlDescontarStock = """
            UPDATE productos
            SET stock = stock - ?
            WHERE id_producto = ?
              AND stock >= ?
            """;

        String sqlMovimiento = """
            INSERT INTO movimientos_inventario
            (id_producto, id_usuario, tipo, cantidad,
             unidad_medida, referencia, observaciones, id_venta)
            VALUES (?, 1, 'venta', ?, 'unidad', ?, ?, ?)
            """;

        for (int i = 0; i < modelo.getRowCount(); i++) {

            String codigo =
                modelo.getValueAt(i, 0).toString();

            int cantidad = Integer.parseInt(
                modelo.getValueAt(i, 3).toString()
            );

            java.math.BigDecimal precio =
                new java.math.BigDecimal(
                    modelo.getValueAt(i, 2)
                        .toString().replace(",", ".")
                );

            java.math.BigDecimal subtotal =
                precio.multiply(
                    java.math.BigDecimal.valueOf(cantidad)
                );

            int idProducto;
            java.math.BigDecimal stock;

            try (java.sql.PreparedStatement ps =
                    con.prepareStatement(sqlBuscarProducto)) {

                ps.setString(1, codigo);

                try (java.sql.ResultSet rs =
                        ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new java.sql.SQLException(
                            "Ya no existe el producto: " + codigo
                        );
                    }

                    idProducto = rs.getInt("id_producto");
                    stock = rs.getBigDecimal("stock");
                }
            }

            if (stock == null
                    || stock.compareTo(
                        java.math.BigDecimal.valueOf(cantidad)
                    ) < 0) {

                throw new java.sql.SQLException(
                    "Stock insuficiente para: " +
                    modelo.getValueAt(i, 1)
                );
            }

            // Guardar detalle de la venta.
            try (java.sql.PreparedStatement ps =
                    con.prepareStatement(sqlDetalle)) {

                ps.setInt(1, idVenta);
                ps.setInt(2, idProducto);
                ps.setBigDecimal(
                    3, java.math.BigDecimal.valueOf(cantidad)
                );
                ps.setBigDecimal(4, precio);
                ps.setBigDecimal(5, subtotal);
                ps.executeUpdate();
            }

            // Descontar inventario.
            try (java.sql.PreparedStatement ps =
                    con.prepareStatement(sqlDescontarStock)) {

                ps.setBigDecimal(
                    1, java.math.BigDecimal.valueOf(cantidad)
                );
                ps.setInt(2, idProducto);
                ps.setBigDecimal(
                    3, java.math.BigDecimal.valueOf(cantidad)
                );

                if (ps.executeUpdate() != 1) {
                    throw new java.sql.SQLException(
                        "No se pudo actualizar el stock de: " +
                        modelo.getValueAt(i, 1)
                    );
                }
            }

            // Registrar movimiento de inventario.
            try (java.sql.PreparedStatement ps =
                    con.prepareStatement(sqlMovimiento)) {

                ps.setInt(1, idProducto);
                ps.setBigDecimal(
                    2, java.math.BigDecimal.valueOf(cantidad)
                );
                ps.setString(3, numeroTicket);
                ps.setString(4, "Salida por venta");
                ps.setInt(5, idVenta);
                ps.executeUpdate();
            }
        }

        // 4. Actualizar ingresos de la caja.
        String sqlActualizarCaja = """
            UPDATE cajas
            SET total_ingresos = total_ingresos + ?
            WHERE id_caja = ? AND estado = 'abierta'
            """;

        try (java.sql.PreparedStatement ps =
                con.prepareStatement(sqlActualizarCaja)) {

            ps.setBigDecimal(1, total);
            ps.setInt(2, idCaja);

            if (ps.executeUpdate() != 1) {
                throw new java.sql.SQLException(
                    "No se pudo actualizar la caja."
                );
            }
        }

        // 5. Confirmar todos los cambios.
        con.commit();
        
StringBuilder ticket = new StringBuilder();

ticket.append("       MR CAKE\n");
ticket.append("     COMPROBANTE DE VENTA\n");
ticket.append("--------------------------------\n");
ticket.append("Ticket: ").append(numeroTicket).append("\n");
ticket.append("Fecha: ")
      .append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
      .format(new java.util.Date())).append("\n");
ticket.append("--------------------------------\n");

ticket.append(String.format("%-16s %3s %7s\n",
        "Producto", "Cant", "Importe"));

for (int i = 0; i < modelo.getRowCount(); i++) {
    String nombre = modelo.getValueAt(i, 1).toString();
    int cantidad = Integer.parseInt(
            modelo.getValueAt(i, 3).toString()
    );
    String subtotal = modelo.getValueAt(i, 4).toString();

    ticket.append(nombre).append("\n");
    ticket.append(String.format("%20s %7s\n",
            "x" + cantidad, "$" + subtotal));
}

ticket.append("--------------------------------\n");
ticket.append("TOTAL:       $").append(formato.format(total)).append("\n");
ticket.append("RECIBIDO:    $").append(formato.format(recibido)).append("\n");
ticket.append("CAMBIO:      $").append(formato.format(cambio)).append("\n");
ticket.append("--------------------------------\n");
ticket.append("     ¡Gracias por su compra!\n\n\n");

imprimirTicket(ticket.toString());


       modelo.setRowCount(0);
txtMontoRecibido.setText("");
actualizarTotal();
java.awt.EventQueue.invokeLater(() -> {
    txtCodigoBarras.requestFocusInWindow();
});

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "¡Venta guardada correctamente!\n" +
            "Ticket: " + numeroTicket + "\n" +
            "Total: $" + formato.format(total) + "\n" +
            "Recibido: $" + formato.format(recibido) + "\n" +
            "Cambio: $" + formato.format(cambio)
        );

    } catch (Exception ex) {

        if (con != null) {
            try {
                con.rollback();
            } catch (java.sql.SQLException errorRollback) {
                ex.addSuppressed(errorRollback);
            }
        }

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "No se pudo guardar la venta.\n" +
            "No se confirmaron los cambios.\n\n" +
            ex.getMessage()
        );

    } finally {

        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (java.sql.SQLException ex) {
                logger.log(
                    java.util.logging.Level.WARNING,
                    "Error al cerrar la conexión", ex
                );
            }
        }
    }
    
}
private void reimprimirTodosLosTickets() {

    int confirmar = javax.swing.JOptionPane.showConfirmDialog(
        this,
        "Se imprimirán nuevamente todos los tickets guardados.\n"
        + "Esto no registrará ventas nuevas ni cambiará el inventario.\n\n"
        + "¿Deseás continuar?",
        "Reimprimir todos los tickets",
        javax.swing.JOptionPane.YES_NO_OPTION
    );

    if (confirmar != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }

    String sqlVentas = """
        SELECT id_venta, numero_ticket, subtotal, descuento,
               total, monto_recibido, cambio, estado
        FROM ventas
        ORDER BY id_venta ASC
        """;

    String sqlDetalles = """
        SELECT p.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal
        FROM detalle_ventas dv
        INNER JOIN productos p ON p.id_producto = dv.id_producto
        WHERE dv.id_venta = ?
        ORDER BY dv.id_detalle_venta ASC
        """;

    StringBuilder todosLosTickets = new StringBuilder();
    int cantidadTickets = 0;

    try (java.sql.Connection con = Conexion.conectar();
         java.sql.PreparedStatement psVentas =
             con.prepareStatement(sqlVentas);
         java.sql.ResultSet rsVentas = psVentas.executeQuery()) {

        while (rsVentas.next()) {

            int idVenta = rsVentas.getInt("id_venta");
            String numeroTicket = rsVentas.getString("numero_ticket");
            String estado = rsVentas.getString("estado");

            todosLosTickets.append("\n\n");
            todosLosTickets.append("          MR CAKE\n");
            todosLosTickets.append("      COMPROBANTE DE VENTA\n");
            todosLosTickets.append("--------------------------------\n");
            todosLosTickets.append("Ticket: ")
                           .append(numeroTicket).append("\n");
            todosLosTickets.append("Venta ID: ")
                           .append(idVenta).append("\n");

            if (estado != null && estado.equalsIgnoreCase("anulada")) {
                todosLosTickets.append("*** VENTA ANULADA ***\n");
            }

            todosLosTickets.append("--------------------------------\n");
            todosLosTickets.append(
                String.format("%-16s %3s %7s%n",
                    "Producto", "Cant", "Importe")
            );

            try (java.sql.PreparedStatement psDetalle =
                     con.prepareStatement(sqlDetalles)) {

                psDetalle.setInt(1, idVenta);

                try (java.sql.ResultSet rsDetalle =
                         psDetalle.executeQuery()) {

                    while (rsDetalle.next()) {

                        String nombre = rsDetalle.getString("nombre");
                        java.math.BigDecimal cantidad =
                            rsDetalle.getBigDecimal("cantidad");
                        java.math.BigDecimal precio =
                            rsDetalle.getBigDecimal("precio_unitario");
                        java.math.BigDecimal subtotalProducto =
                            rsDetalle.getBigDecimal("subtotal");

                        todosLosTickets.append(nombre).append("\n");

                        todosLosTickets.append(
                            String.format("%20s %7s%n",
                                "x" + cantidad.stripTrailingZeros().toPlainString(),
                                "$" + formato.format(subtotalProducto))
                        );
                    }
                }
            }

            todosLosTickets.append("--------------------------------\n");
            todosLosTickets.append("SUBTOTAL:    $")
                .append(formato.format(rsVentas.getBigDecimal("subtotal")))
                .append("\n");
            todosLosTickets.append("DESCUENTO:   $")
                .append(formato.format(rsVentas.getBigDecimal("descuento")))
                .append("\n");
            todosLosTickets.append("TOTAL:       $")
                .append(formato.format(rsVentas.getBigDecimal("total")))
                .append("\n");
            todosLosTickets.append("RECIBIDO:    $")
                .append(formato.format(rsVentas.getBigDecimal("monto_recibido")))
                .append("\n");
            todosLosTickets.append("CAMBIO:      $")
                .append(formato.format(rsVentas.getBigDecimal("cambio")))
                .append("\n");
            todosLosTickets.append("--------------------------------\n");
            todosLosTickets.append("      ¡Gracias por su compra!\n");
            todosLosTickets.append("\n\n\n");

            cantidadTickets++;
        }

        if (cantidadTickets == 0) {
            javax.swing.JOptionPane.showMessageDialog(
                this, "No hay ventas guardadas para imprimir."
            );
            return;
        }

        imprimirTicket(todosLosTickets.toString());

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Se enviaron " + cantidadTickets
            + " tickets a la impresora.\n"
            + "Revisá la salida de la POS58 Printer."
        );

    } catch (Exception ex) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "No se pudieron recuperar los tickets:\n"
            + ex.getMessage()
        );
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtCodigoBarras = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCarrito = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        btnQuitar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        txtMontoRecibido = new javax.swing.JTextField();
        lblCambio = new javax.swing.JLabel();
        btnFinalizarVenta = new javax.swing.JButton();
        btnVolverInicio = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        txtCodigoBarras.addActionListener(this::txtCodigoBarrasActionPerformed);

        btnAgregar.setText("Agregar producto");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);

        tablaCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Producto", "Precio", "Cantidad", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(tablaCarrito);

        lblTotal.setText("Total: $0.00");

        btnQuitar.setText("Quitar producto");
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);

        btnLimpiar.setText("Limpiar carrito");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        txtMontoRecibido.setText("Monto recibido:");
        txtMontoRecibido.setMaximumSize(new java.awt.Dimension(120, 25));
        txtMontoRecibido.setMinimumSize(new java.awt.Dimension(120, 25));

        lblCambio.setText("Cambio: $0.00");

        btnFinalizarVenta.setText("Finalizar venta");
        btnFinalizarVenta.setMaximumSize(new java.awt.Dimension(120, 25));
        btnFinalizarVenta.setMinimumSize(new java.awt.Dimension(120, 25));

        btnVolverInicio.setText("Volver al inicio");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnAgregar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                        .addComponent(btnQuitar)
                        .addGap(73, 73, 73)
                        .addComponent(btnLimpiar)
                        .addGap(184, 184, 184))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtMontoRecibido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblCambio)
                                    .addComponent(btnFinalizarVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(lblTotal)))
                        .addGap(155, 155, 155))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnVolverInicio)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(txtMontoRecibido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCambio)
                        .addGap(18, 18, 18)
                        .addComponent(btnFinalizarVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(lblTotal)
                        .addGap(27, 27, 27))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpiar)
                    .addComponent(btnQuitar)
                    .addComponent(btnAgregar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                .addComponent(btnVolverInicio)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCodigoBarrasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoBarrasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoBarrasActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        agregarProducto();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        quitarProducto();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarCarrito();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnFinalizarVenta;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnVolverInicio;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCambio;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tablaCarrito;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JTextField txtMontoRecibido;
    // End of variables declaration//GEN-END:variables
public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(() -> {
        new FrmVentas().setVisible(true);
    });
}

}

