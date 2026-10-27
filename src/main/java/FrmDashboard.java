/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author lcore
 */
public class FrmDashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmDashboard.class.getName());
    private FrmProductos productosAbiertos;
    private FrmVentas ventasAbiertas;
    private boolean cerrandoSesion;

    /**
     * Creates new form FrmDashboard
     */
    public FrmDashboard() {
        initComponents();
        configurarTablaVentas();
        configurarNavegacion();
        cargarResumen();
        cargarUltimasVentas();
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (!cerrandoSesion && !(e.getOppositeWindow() instanceof javax.swing.JDialog)) {
                    actualizarDashboard();
                }
            }
        });
    }

    private void configurarTablaVentas() {
        javax.swing.table.DefaultTableModel modelo =
                new javax.swing.table.DefaultTableModel(
                        new Object[][]{},
                        new String[]{"Número de ticket", "Fecha", "Total", "Estado"}) {
                    @Override
                    public boolean isCellEditable(int fila, int columna) {
                        return false;
                    }
                };
        tablaUltimasVentas.setModel(modelo);
        tablaUltimasVentas.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaUltimasVentas.setFillsViewportHeight(true);
    }

    private void configurarNavegacion() {
        btnInicio.addActionListener(e -> actualizarDashboard());
        btnProductos.addActionListener(e -> abrirProductos());
        btnVentas.addActionListener(e -> abrirVentas());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
    }

    private void actualizarDashboard() {
        cargarResumen();
        cargarUltimasVentas();
    }

    private void cargarResumen() {
        String sqlVentas = """
            SELECT
                COALESCE(SUM(CASE
                    WHEN DATE(fecha) = CURRENT_DATE() THEN total
                    ELSE 0 END), 0) AS ventas_hoy,
                COALESCE(SUM(CASE
                    WHEN YEAR(fecha) = YEAR(CURRENT_DATE())
                     AND MONTH(fecha) = MONTH(CURRENT_DATE()) THEN total
                    ELSE 0 END), 0) AS ventas_mes,
                COUNT(*) AS cantidad_ventas,
                COALESCE(AVG(total), 0) AS ticket_promedio
            FROM ventas
            WHERE estado = 'completada'
            """;
        String sqlStockBajo = """
            SELECT COUNT(*) AS cantidad
            FROM productos
            WHERE stock <= stock_minimo
            """;

        try (java.sql.Connection con = Conexion.conectar();
             java.sql.PreparedStatement ps = con.prepareStatement(sqlVentas);
             java.sql.ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                java.text.NumberFormat moneda =
                        java.text.NumberFormat.getCurrencyInstance(
                                java.util.Locale.US);
                lblVentasHoy.setText("Ventas de hoy: "
                        + moneda.format(rs.getBigDecimal("ventas_hoy")));
                lblVentasMes.setText("Ventas del mes: "
                        + moneda.format(rs.getBigDecimal("ventas_mes")));
                lblCantidadVentas.setText("Número de ventas: "
                        + rs.getLong("cantidad_ventas"));
                lblTicketPromedio.setText("Ticket promedio: "
                        + moneda.format(rs.getBigDecimal("ticket_promedio")));
            }
        } catch (java.sql.SQLException ex) {
            mostrarError("No se pudieron cargar las métricas de ventas.", ex);
            return;
        }

        try (java.sql.Connection con = Conexion.conectar();
             java.sql.PreparedStatement ps = con.prepareStatement(sqlStockBajo);
             java.sql.ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                lblStockBajo.setText("Productos con stock bajo: "
                        + rs.getLong("cantidad"));
            }
        } catch (java.sql.SQLException ex) {
            mostrarError("No se pudo cargar el conteo de stock bajo.", ex);
        }
    }

    private void cargarUltimasVentas() {
        String sql = """
            SELECT numero_ticket, fecha, total, estado
            FROM ventas
            ORDER BY fecha DESC, id_venta DESC
            LIMIT 10
            """;

        javax.swing.table.DefaultTableModel modelo =
                (javax.swing.table.DefaultTableModel) tablaUltimasVentas.getModel();
        modelo.setRowCount(0);

        try (java.sql.Connection con = Conexion.conectar();
             java.sql.PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            java.text.NumberFormat moneda =
                    java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US);
            java.time.format.DateTimeFormatter formatoFecha =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                java.sql.Timestamp fecha = rs.getTimestamp("fecha");
                modelo.addRow(new Object[]{
                    rs.getString("numero_ticket"),
                    fecha == null ? "" : fecha.toLocalDateTime().format(formatoFecha),
                    moneda.format(rs.getBigDecimal("total")),
                    rs.getString("estado")
                });
            }
        } catch (java.sql.SQLException ex) {
            mostrarError("No se pudieron cargar las últimas ventas.", ex);
        }
    }

    private void abrirProductos() {
        if (productosAbiertos == null || !productosAbiertos.isDisplayable()) {
            productosAbiertos = new FrmProductos(this);
            FrmProductos ventana = productosAbiertos;
            ventana.setLocationRelativeTo(this);
            ventana.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) {
                    if (productosAbiertos == ventana) productosAbiertos = null;
                    if (!cerrandoSesion) actualizarDashboard();
                }
            });
        }
        mostrarVentana(productosAbiertos);
    }

    private void abrirVentas() {
        if (ventasAbiertas == null || !ventasAbiertas.isDisplayable()) {
            ventasAbiertas = new FrmVentas(this);
            FrmVentas ventana = ventasAbiertas;
            ventana.setLocationRelativeTo(this);
            ventana.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) {
                    if (ventasAbiertas == ventana) ventasAbiertas = null;
                    if (!cerrandoSesion) actualizarDashboard();
                }
            });
        }
        mostrarVentana(ventasAbiertas);
    }

    void abrirVentasDesdeProductos(FrmProductos productos) {
        if (productosAbiertos == productos) {
            abrirVentas();
        }
    }

    private void mostrarVentana(javax.swing.JFrame ventana) {
        ventana.setExtendedState(ventana.getExtendedState() & ~java.awt.Frame.ICONIFIED);
        ventana.setVisible(true);
        ventana.toFront();
        ventana.requestFocus();
    }

    void mostrarDashboard() {
        actualizarDashboard();
        mostrarVentana(this);
    }

    private void cerrarSesion() {
        cerrandoSesion = true;
        if (productosAbiertos != null) productosAbiertos.dispose();
        if (ventasAbiertas != null) ventasAbiertas.dispose();
        new FrmLogin().setVisible(true);
        dispose();
    }

    private void mostrarError(String mensaje, java.sql.SQLException ex) {
        logger.log(java.util.logging.Level.SEVERE, mensaje, ex);
        javax.swing.JOptionPane.showMessageDialog(
                this,
                mensaje + "\n" + ex.getMessage(),
                "Error de base de datos",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        pnlMenu = new javax.swing.JPanel();
        btnInicio = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        pnlContenido = new javax.swing.JPanel();
        lblResumen = new javax.swing.JLabel();
        lblVentasHoy = new javax.swing.JLabel();
        lblVentasMes = new javax.swing.JLabel();
        lblTicketPromedio = new javax.swing.JLabel();
        lblCantidadVentas = new javax.swing.JLabel();
        lblStockBajo = new javax.swing.JLabel();
        lblUltimasVentas = new javax.swing.JLabel();
        jScrollPaneVentas = new javax.swing.JScrollPane();
        tablaUltimasVentas = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblTitulo.setText("Mr Cake");

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDescripcion.setText("Panel de administración");

        pnlMenu.setPreferredSize(new java.awt.Dimension(170, 300));

        btnInicio.setText("Dashboard");

        btnProductos.setText("Productos");

        btnVentas.setText("Ventas");

        btnInventario.setText("Inventario");

        btnCerrarSesion.setText("Cerrar sesión");

        javax.swing.GroupLayout pnlMenuLayout = new javax.swing.GroupLayout(pnlMenu);
        pnlMenu.setLayout(pnlMenuLayout);
        pnlMenuLayout.setHorizontalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnVentas)
                    .addComponent(btnInicio)
                    .addComponent(btnProductos)
                    .addComponent(btnInventario)
                    .addComponent(btnCerrarSesion))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        pnlMenuLayout.setVerticalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(btnInicio)
                .addGap(18, 18, 18)
                .addComponent(btnProductos)
                .addGap(18, 18, 18)
                .addComponent(btnVentas)
                .addGap(18, 18, 18)
                .addComponent(btnInventario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addComponent(btnCerrarSesion)
                .addGap(15, 15, 15))
        );

        lblResumen.setText("Resumen del negocio");

        lblVentasHoy.setText("Ventas de hoy: $0.00");

        lblVentasMes.setText("Ventas del mes: $0.00");

        lblTicketPromedio.setText("Ticket promedio: $0.00");

        lblCantidadVentas.setText("Número de ventas: 0");

        lblStockBajo.setText("Productos con stock bajo: 0");

        lblUltimasVentas.setText("Últimas ventas");

        tablaUltimasVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Número de ticket", "Fecha", "Total", "Estado"}
        ));
        jScrollPaneVentas.setViewportView(tablaUltimasVentas);

        javax.swing.GroupLayout pnlContenidoLayout = new javax.swing.GroupLayout(pnlContenido);
        pnlContenido.setLayout(pnlContenidoLayout);
        pnlContenidoLayout.setHorizontalGroup(
            pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContenidoLayout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addComponent(lblTicketPromedio))
                    .addGroup(pnlContenidoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblVentasHoy)
                            .addComponent(lblResumen)
                            .addComponent(lblCantidadVentas))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVentasMes)
                    .addComponent(lblStockBajo))
                .addContainerGap(104, Short.MAX_VALUE))
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblUltimasVentas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlContenidoLayout.setVerticalGroup(
            pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblResumen)
                .addGap(18, 18, 18)
                .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVentasHoy)
                    .addComponent(lblVentasMes))
                .addGap(75, 75, 75)
                .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantidadVentas)
                    .addComponent(lblStockBajo))
                .addGap(51, 51, 51)
                .addComponent(lblTicketPromedio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUltimasVentas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDescripcion)
                            .addComponent(pnlMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(pnlContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDescripcion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(113, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(pnlContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnVentas;
    private javax.swing.JLabel lblCantidadVentas;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblResumen;
    private javax.swing.JLabel lblStockBajo;
    private javax.swing.JLabel lblTicketPromedio;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUltimasVentas;
    private javax.swing.JLabel lblVentasHoy;
    private javax.swing.JLabel lblVentasMes;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JScrollPane jScrollPaneVentas;
    private javax.swing.JTable tablaUltimasVentas;
    // End of variables declaration//GEN-END:variables
}
