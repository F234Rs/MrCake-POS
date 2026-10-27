
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection conexion = Conexion.conectar()) {
            JOptionPane.showMessageDialog(
                null,
                "¡Conexión exitosa con MariaDB y Mr Cake!"
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error de conexión:\n" + e.getMessage()
            );
        }
    }
}
