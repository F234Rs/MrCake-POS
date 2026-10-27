import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

/** Crea el primer administrador en una base de demostración vacía. */
public class CrearAdministrador {
    public static void main(String[] args) throws Exception {
        Console consola = System.console();
        if (consola == null) {
            throw new IllegalStateException("Ejecutá esta herramienta en una terminal interactiva.");
        }

        String url = consola.readLine("URL JDBC de MariaDB [jdbc:mariadb://127.0.0.1:3307/mrcake]: ").trim();
        if (url.isEmpty()) url = "jdbc:mariadb://127.0.0.1:3307/mrcake";
        String usuarioBd = consola.readLine("Usuario de MariaDB: ").trim();
        char[] claveBd = consola.readPassword("Contraseña de MariaDB (vacía si no tiene): ");
        char[] clave = null;
        char[] confirmacion = null;
        try {
            if (usuarioBd.isEmpty() || claveBd == null) throw new IllegalArgumentException("Ingresá el usuario de MariaDB.");
            String nombre = consola.readLine("Nombre del administrador: ").trim();
            String usuario = consola.readLine("Usuario para iniciar sesión: ").trim();
            clave = consola.readPassword("Contraseña nueva (mínimo 12 caracteres): ");
            confirmacion = consola.readPassword("Repetí la contraseña: ");
            if (nombre.isEmpty() || usuario.isEmpty() || clave == null || confirmacion == null
                    || clave.length < 12 || !Arrays.equals(clave, confirmacion)) {
                throw new IllegalArgumentException("Los datos están incompletos o las contraseñas no coinciden.");
            }
            String claveTexto = new String(clave);
            if (claveTexto.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("La contraseña supera los 72 bytes admitidos por BCrypt.");
            }
            String hash = BCrypt.hashpw(claveTexto, BCrypt.gensalt(12));

            try (Connection con = DriverManager.getConnection(url, usuarioBd, new String(claveBd));
                 PreparedStatement verificar = con.prepareStatement("SELECT COUNT(*) FROM usuarios");
                 ResultSet rs = verificar.executeQuery()) {
                if (!rs.next() || rs.getLong(1) != 0) {
                    throw new IllegalStateException("La tabla usuarios debe estar vacía; no se creó ningún usuario.");
                }
                try (PreparedStatement insertar = con.prepareStatement(
                        "INSERT INTO usuarios (id_usuario, nombre, usuario, password, rol, estado) VALUES (1, ?, ?, ?, 'administrador', 'activo')")) {
                    insertar.setString(1, nombre);
                    insertar.setString(2, usuario);
                    insertar.setString(3, hash);
                    insertar.executeUpdate();
                }
            }
            consola.printf("Administrador inicial creado con ID 1.%n");
        } finally {
            if (claveBd != null) Arrays.fill(claveBd, '\0');
            if (clave != null) Arrays.fill(clave, '\0');
            if (confirmacion != null) Arrays.fill(confirmacion, '\0');
        }
    }
}
