package datos;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class ConexionBD {
	private static Connection con = null;

	// Nos conectamos a la base de datos leyendo config.properties desde la raíz
	public static Connection obtenerConexion() {
		try {
			if (con == null) {
				// Con esto determinamos cuándo finaliza el programa para cerrar la conexión
				Runtime.getRuntime().addShutdownHook(new MiShDwnHook());
				
				// Usamos Properties clásico para leer de la raíz del proyecto
				Properties prop = new Properties();
				try (FileInputStream input = new FileInputStream("jdbc.properties")) {
					prop.load(input);
				}
				
				String driver = prop.getProperty("driver");
				String url = prop.getProperty("url");
				String usr = prop.getProperty("usr");
				String pwd = prop.getProperty("pwd");
				String schema = prop.getProperty("schema", "public");
				
				Class.forName(driver);
				con = DriverManager.getConnection(url, usr, pwd);
				
				// Seteamos el esquema por defecto en la sesión de Postgres
				try (Statement statement = con.createStatement()) {
					statement.execute("set search_path to '" + schema + "'"); //
				}
			}
			return con;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Error al crear la conexion", ex);
		}
	}

	public static class MiShDwnHook extends Thread {
		// Justo antes de finalizar el programa la JVM invocará este método para cerrar la conexión
		@Override
		public void run() {
			try {
				if (con != null && !con.isClosed()) {
					con.close(); //
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
	}
}
