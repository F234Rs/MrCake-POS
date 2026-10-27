import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;  
    

public class FrmProductos extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmProductos.class.getName());
    private final FrmDashboard dashboard;
    private boolean cambiandoPantalla;
    private boolean actualizandoTabla;
    private boolean productoCambioExterno;
    private FrmVentas ventasIndependiente;


    private java.math.BigDecimal leerDecimal(String valor, String campo)
        throws IllegalArgumentException {
    try {
        java.math.BigDecimal numero =
                new java.math.BigDecimal(valor.trim());

        if (numero.signum() < 0) {
            throw new IllegalArgumentException(
                    campo + " no puede ser negativo.");
        }

        return numero;

    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                campo + " debe ser un número válido.");
    }
}

private Integer obtenerIdCategoria(
        Connection conexion, String categoria)
        throws SQLException {

    if (categoria.trim().isEmpty()) {
        return null;
    }

    String sql;

    try {
        int id = Integer.parseInt(categoria.trim());
        sql = "SELECT id_categoria FROM categorias WHERE id_categoria = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        }

        throw new IllegalArgumentException(
                "No existe una categoría con ese ID.");

    } catch (NumberFormatException e) {
        sql = "SELECT id_categoria FROM categorias WHERE nombre = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, categoria.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        }

        throw new IllegalArgumentException(
                "No existe esa categoría. Escriba su nombre exacto o su ID.");
    }
}

private void refrescarProductos() {
    cargarProductos();
}

private void cargarProductos() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaProductos.getModel();

    modelo.setRowCount(0);

    String sql = """
        SELECT p.id_producto,
               p.nombre,
               c.nombre AS categoria,
               p.precio_venta,
               p.stock
        FROM productos p
        LEFT JOIN categorias c
            ON p.id_categoria = c.id_categoria
        ORDER BY p.id_producto
        """;

    try (Connection conexion = Conexion.conectar();
         PreparedStatement ps = conexion.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Object[] fila = {
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getBigDecimal("precio_venta"),
                rs.getBigDecimal("stock")
            };

            modelo.addRow(fila);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Error al cargar los productos:\n" + e.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
    }
}    

 public FrmProductos() {
    this(null);
 }

 public FrmProductos(FrmDashboard dashboard) {
    this.dashboard = dashboard;
    initComponents();
    btnVolverInicio.addActionListener(e -> volverAlInicio());
    cargarProductos();
    addWindowFocusListener(new java.awt.event.WindowAdapter() {
        @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
            if (!(e.getOppositeWindow() instanceof javax.swing.JDialog)) {
                refrescarAlVolver();
            }
        }
    });

    tablaProductos.getSelectionModel()
        .addListSelectionListener(e -> {
            if (!actualizandoTabla && !e.getValueIsAdjusting()) {
                int fila = tablaProductos.getSelectedRow();

                if (fila != -1) {
                    productoCambioExterno = false;
                    jTextField1.setText(
                        tablaProductos.getValueAt(fila, 0).toString());

                    jTextField2.setText(
                        tablaProductos.getValueAt(fila, 1).toString());

                    jTextField3.setText(
                        tablaProductos.getValueAt(fila, 2) == null
                        ? ""
                        : tablaProductos.getValueAt(fila, 2).toString());

                    jTextField4.setText(
                        tablaProductos.getValueAt(fila, 3) == null
                        ? ""
                        : tablaProductos.getValueAt(fila, 3).toString());

                    jTextField5.setText(
                        tablaProductos.getValueAt(fila, 4).toString());
                }
            }
        });
}

 private void refrescarAlVolver() {
    int fila = tablaProductos.getSelectedRow();
    Object[] anterior = null;
    if (fila >= 0) {
        anterior = new Object[tablaProductos.getColumnCount()];
        for (int i = 0; i < anterior.length; i++) {
            anterior[i] = tablaProductos.getValueAt(fila, i);
        }
    }
    actualizandoTabla = true;
    try {
        cargarProductos();
        if (anterior != null) {
            for (int i = 0; i < tablaProductos.getRowCount(); i++) {
                if (java.util.Objects.equals(anterior[0], tablaProductos.getValueAt(i, 0))) {
                    for (int j = 1; j < anterior.length; j++) {
                        if (!java.util.Objects.equals(anterior[j], tablaProductos.getValueAt(i, j))) {
                            productoCambioExterno = true;
                        }
                    }
                    tablaProductos.setRowSelectionInterval(i, i);
                    if (productoCambioExterno) {
                        tablaProductos.clearSelection();
                    }
                    break;
                }
            }
        }
    } finally {
        actualizandoTabla = false;
    }
 }

 private void volverAlInicio() {
    if (dashboard != null) {
        dashboard.mostrarDashboard();
        return;
    }
    if (cambiandoPantalla) {
        return;
    }
    cambiandoPantalla = true;
    if (dashboard == null) {
        FrmDashboard inicio = new FrmDashboard();
        inicio.setLocationRelativeTo(null);
        inicio.setVisible(true);
    }
    dispose();
 }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        txtId = new javax.swing.JLabel();
        txtNombre = new javax.swing.JLabel();
        txtCategoria = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JLabel();
        txtStock = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaProductos = new javax.swing.JTable();
        txtCodigoBarras = new javax.swing.JTextField();
        btnEscanear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnVentas = new javax.swing.JButton();
        btnVolverInicio = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblTitulo.setText("PRODUCTO");

        txtId.setText("ID Producto");

        txtNombre.setText("Nombre ");

        txtCategoria.setText("Categoria ");

        txtPrecio.setText("Precio de venta ");

        txtStock.setText("Stock disponible");

        btnGuardar.setText("Guardar ");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnModificar.setText("Modificar");
        btnModificar.addActionListener(this::btnModificarActionPerformed);

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        tablaProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Categoria", "Precio", "Stock"
            }
        ));
        jScrollPane1.setViewportView(tablaProductos);

        btnEscanear.setText("Escanear");
        btnEscanear.addActionListener(this::btnEscanearActionPerformed);

        jLabel2.setText("Codigo de barra");

        btnVentas.setText("Ir a pestaña de ventas");
        btnVentas.addActionListener(this::btnVentasActionPerformed);

        btnVolverInicio.setText("Volver al inicio");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGuardar)
                .addGap(27, 27, 27)
                .addComponent(btnModificar)
                .addGap(55, 55, 55)
                .addComponent(btnEliminar)
                .addGap(41, 41, 41)
                .addComponent(btnLimpiar)
                .addGap(23, 23, 23))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnVolverInicio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(153, 153, 153)
                                .addComponent(lblTitulo))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(99, 99, 99)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(1, 1, 1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtStock)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtPrecio)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtCategoria)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(txtNombre)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(txtId)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEscanear)
                            .addComponent(btnVentas))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitulo)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtId)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombre)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCategoria)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrecio)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVentas))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStock)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEscanear, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnModificar)
                    .addComponent(btnEliminar)
                    .addComponent(btnLimpiar))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVolverInicio)
                .addContainerGap(300, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
      if (productoCambioExterno) {
        JOptionPane.showMessageDialog(this,
                "El producto cambió mientras editabas. Volvé a seleccionarlo para cargar los datos actuales.");
        return;
      }
      int fila = tablaProductos.getSelectedRow();

    if (fila == -1) {
        JOptionPane.showMessageDialog(
                this, "Seleccione un producto de la tabla.");
        return;
    }

    int id = Integer.parseInt(
            tablaProductos.getValueAt(fila, 0).toString());

    String nombre = jTextField2.getText().trim();
    String categoria = jTextField3.getText().trim();
    String precio = jTextField4.getText().trim();
    String stock = jTextField5.getText().trim();
    String codigo = txtCodigoBarras.getText().trim();

    if (nombre.isEmpty() || precio.isEmpty() || stock.isEmpty()) {
        JOptionPane.showMessageDialog(
                this, "Complete nombre, precio y stock.");
        return;
    }

    try {
        java.math.BigDecimal precioNum =
                leerDecimal(precio, "El precio");

        java.math.BigDecimal stockNum =
                leerDecimal(stock, "El stock");

        if (precioNum.scale() > 2 || stockNum.scale() > 3) {
            JOptionPane.showMessageDialog(
                    this, "El precio admite 2 decimales y el stock 3.");
            return;
        }

        String sql = """
            UPDATE productos
            SET nombre = ?, precio_venta = ?, stock = ?,
                id_categoria = ?, codigo_barras = COALESCE(?, codigo_barras)
            WHERE id_producto = ?
            """;

        try (Connection conexion = Conexion.conectar()) {
            Integer idCategoria =
                    obtenerIdCategoria(conexion, categoria);

            try (PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

                ps.setString(1, nombre);
                ps.setBigDecimal(2, precioNum);
                ps.setBigDecimal(3, stockNum);
                ps.setObject(4, idCategoria);
                ps.setString(5, codigo.isEmpty() ? null : codigo);
                ps.setInt(6, id);

                int filas = ps.executeUpdate();

                if (filas == 0) {
                    JOptionPane.showMessageDialog(
                            this, "No se encontró el producto.");
                    return;
                }
            }
        }

        refrescarProductos();

        JOptionPane.showMessageDialog(
                this, "Producto actualizado correctamente.");

    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this, "Error al modificar:\n" + e.getMessage());
    }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
      jTextField1.setText("");
jTextField2.setText("");
jTextField3.setText("");
jTextField4.setText("");
jTextField5.setText("");
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
    String nombre = jTextField2.getText().trim();
    String categoria = jTextField3.getText().trim();
    String precio = jTextField4.getText().trim();
    String stock = jTextField5.getText().trim();
    String codigo = txtCodigoBarras.getText().trim();

    if (nombre.isEmpty() || precio.isEmpty() || stock.isEmpty()) {
        JOptionPane.showMessageDialog(
                this, "Complete nombre, precio y stock.");
        return;
    }

    try {
        java.math.BigDecimal precioNum =
                leerDecimal(precio, "El precio");

        java.math.BigDecimal stockNum =
                leerDecimal(stock, "El stock");

        if (precioNum.scale() > 2 || stockNum.scale() > 3) {
            JOptionPane.showMessageDialog(
                    this, "El precio admite 2 decimales y el stock 3.");
            return;
        }

        String sql = """
            INSERT INTO productos
            (nombre, precio_venta, stock, id_categoria,
             codigo_barras, stock_minimo)
            VALUES (?, ?, ?, ?, ?, 0)
            """;

        try (Connection conexion = Conexion.conectar()) {
            Integer idCategoria =
                    obtenerIdCategoria(conexion, categoria);

            try (PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

                ps.setString(1, nombre);
                ps.setBigDecimal(2, precioNum);
                ps.setBigDecimal(3, stockNum);
                ps.setObject(4, idCategoria);
                ps.setString(5, codigo.isEmpty() ? null : codigo);

                ps.executeUpdate();
            }
        }

        refrescarProductos();

        JOptionPane.showMessageDialog(
                this, "Producto guardado en MariaDB correctamente.");

    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this, "Error al guardar:\n" + e.getMessage());
    }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
   int fila = tablaProductos.getSelectedRow();

    if (fila == -1) {
        JOptionPane.showMessageDialog(
                this, "Seleccione un producto de la tabla.");
        return;
    }

    int id = Integer.parseInt(
            tablaProductos.getValueAt(fila, 0).toString());

    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que desea eliminar este producto de MariaDB?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    String sql =
            "DELETE FROM productos WHERE id_producto = ?";

    try (Connection conexion = Conexion.conectar();
         PreparedStatement ps = conexion.prepareStatement(sql)) {

        ps.setInt(1, id);

        int filas = ps.executeUpdate();

        if (filas > 0) {
            refrescarProductos();

            JOptionPane.showMessageDialog(
                    this, "Producto eliminado correctamente.");
        } else {
            JOptionPane.showMessageDialog(
                    this, "El producto ya no existe.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "No se pudo eliminar el producto.\n"
                + "Puede estar relacionado con ventas u otros registros.\n\n"
                + e.getMessage()
        );
    }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnEscanearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEscanearActionPerformed

String codigo = txtCodigoBarras.getText().trim();

if (codigo.isEmpty()) {
    JOptionPane.showMessageDialog(
        this,
        "Escanee un código de barras."
    );
    txtCodigoBarras.requestFocus();
    return;
}

String sql = """
    SELECT p.id_producto,
           p.nombre,
           c.nombre AS categoria,
           p.precio_venta,
           p.stock
    FROM productos p
    LEFT JOIN categorias c
        ON p.id_categoria = c.id_categoria
    WHERE p.codigo_barras = ?
    """;

try (Connection conexion = Conexion.conectar();
     PreparedStatement ps = conexion.prepareStatement(sql)) {

    ps.setString(1, codigo);

    try (ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            jTextField1.setText(
                String.valueOf(rs.getInt("id_producto"))
            );

            jTextField2.setText(
                rs.getString("nombre")
            );

            jTextField3.setText(
                rs.getString("categoria") == null
                    ? ""
                    : rs.getString("categoria")
            );

            jTextField4.setText(
                rs.getBigDecimal("precio_venta") == null
                    ? ""
                    : rs.getBigDecimal("precio_venta").toString()
            );

            jTextField5.setText(
                rs.getBigDecimal("stock").toString()
            );

            JOptionPane.showMessageDialog(
                this,
                "Producto encontrado: " + rs.getString("nombre")
            );

        } else {
            JOptionPane.showMessageDialog(
                this,
                "Código no encontrado: " + codigo
            );
        }
    }

} catch (SQLException e) {
    JOptionPane.showMessageDialog(
        this,
        "Error al buscar el producto:\n" + e.getMessage(),
        "Error de base de datos",
        JOptionPane.ERROR_MESSAGE
    );

} finally {
    txtCodigoBarras.setText("");
    txtCodigoBarras.requestFocus();
}

    }//GEN-LAST:event_btnEscanearActionPerformed

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
        if (dashboard != null) {
            dashboard.abrirVentasDesdeProductos(this);
        } else {
            if (ventasIndependiente == null || !ventasIndependiente.isDisplayable()) {
                ventasIndependiente = new FrmVentas();
                ventasIndependiente.setLocationRelativeTo(null);
            }
            ventasIndependiente.setVisible(true);
            ventasIndependiente.toFront();
        }
    }//GEN-LAST:event_btnVentasActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnEscanear;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnVentas;
    private javax.swing.JButton btnVolverInicio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tablaProductos;
    private javax.swing.JLabel txtCategoria;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JLabel txtId;
    private javax.swing.JLabel txtNombre;
    private javax.swing.JLabel txtPrecio;
    private javax.swing.JLabel txtStock;
    // End of variables declaration//GEN-END:variables

public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(() -> {
        new FrmProductos().setVisible(true);
    });
}
}
