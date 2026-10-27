public class FrmLogin extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmLogin.class.getName());


   public FrmLogin() {
    initComponents();
    txtPassword.addActionListener(e -> btnIngresar.doClick());

        
    java.net.URL recurso = getClass().getResource("/imagenes/logo.png");

    if (recurso != null) {
        javax.swing.ImageIcon icono =
                new javax.swing.ImageIcon(recurso);

        java.awt.Image imagen = icono.getImage()
                .getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);

        lblLogo.setIcon(new javax.swing.ImageIcon(imagen));
        lblLogo.setText("");
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    } else {
        lblLogo.setText("Logo no encontrado");
    }

    lblMensaje.setText("");
    setLocationRelativeTo(null);
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnIngresar = new javax.swing.JButton();
        lblMensaje = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitulo.setText("MR CAKE");

        lblDescripcion.setText("Sistema de ventas e inventario");

        lblUsuario.setText("Usuario");

        txtUsuario.addActionListener(this::txtUsuarioActionPerformed);

        lblPassword.setText("Contraseña");

        txtPassword.addActionListener(this::txtPasswordActionPerformed);

        btnIngresar.setText("Iniciar sesión");
        btnIngresar.addActionListener(this::btnIngresarActionPerformed);

        lblLogo.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(224, 224, 224)
                        .addComponent(lblLogo))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblUsuario)
                                .addComponent(txtUsuario)
                                .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPassword))
                            .addComponent(lblMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(86, 86, 86)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblDescripcion)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(50, 50, 50)
                                        .addComponent(lblTitulo)))))))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDescripcion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsuarioActionPerformed
    

    }//GEN-LAST:event_txtUsuarioActionPerformed

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIngresarActionPerformed
     
    String usuario = txtUsuario.getText().trim();
    char[] caracteres = txtPassword.getPassword();
    String clave = new String(caracteres);
    java.util.Arrays.fill(caracteres, '\0');

    lblMensaje.setForeground(java.awt.Color.RED);

    if (usuario.isEmpty() || clave.isEmpty()) {
        lblMensaje.setText("Completa usuario y contraseña.");
        return;
    }

    try (java.sql.Connection con = Conexion.conectar();
         java.sql.PreparedStatement ps = con.prepareStatement(
             "SELECT id_usuario, nombre, password, rol, estado " +
             "FROM usuarios WHERE usuario = ?")) {

        ps.setString(1, usuario);

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                return;
            }

            String hash = rs.getString("password");
            String estado = rs.getString("estado");

            if (!"activo".equals(estado)) {
                lblMensaje.setText("Esta cuenta está inactiva.");
                return;
            }

            if (hash == null || !hash.startsWith("$2")
                    || !org.mindrot.jbcrypt.BCrypt.checkpw(clave, hash)) {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                return;
            }

            lblMensaje.setForeground(new java.awt.Color(0, 128, 0));
            lblMensaje.setText("¡Bienvenido!");

            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Bienvenido a Mr Cake, " + rs.getString("nombre") + "."
            );

            FrmDashboard dashboard = new FrmDashboard();
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
            this.dispose();
        }

    } catch (java.sql.SQLException ex) {
        logger.log(java.util.logging.Level.SEVERE,
                   "Error al validar el inicio de sesión", ex);
        lblMensaje.setText("Error de conexión con la base de datos.");
    } catch (IllegalArgumentException ex) {
        logger.log(java.util.logging.Level.WARNING,
                   "El hash almacenado no es válido", ex);
        lblMensaje.setText("La contraseña guardada no es válida.");
    } finally {
        java.util.Arrays.fill(caracteres, '\0');
    }


    }//GEN-LAST:event_btnIngresarActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPasswordActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
