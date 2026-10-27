
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    public static Connection conectar() throws SQLException {
        String url = "jdbc:mariadb://127.0.0.1:3307/mrcake";
        String usuario = "root";
        String contrasena = "";

        return DriverManager.getConnection(
            url, usuario, contrasena
        );
    }
}
