package datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
	
	private static final String ARCHIVO = "jdbc.properties";

	private static Connection con = null;
	private static boolean hookRegistrado = false;

	// Nos conectamos a la base de datos leyendo config.properties desde la raíz
	public static Connection obtenerConexion() {
		try {
			if (con == null || con.isClosed()) {
				// Usamos Properties clásico para leer de la raíz del proyecto
				Properties prop = cargarPropiedades();

				String driver = prop.getProperty("driver");
				String url = prop.getProperty("url");
				String usr = prop.getProperty("usr");
				String pwd = prop.getProperty("pwd");
				String schema = prop.getProperty("schema", "public");
				
				Class.forName(driver);
				con = DriverManager.getConnection(url, usr, pwd);
				con.setSchema(schema);
				
				// Seteamos el esquema por defecto en la sesión de Postgres
				if (!hookRegistrado) {
					Runtime.getRuntime().addShutdownHook(new Thread(ConexionBD::cerrar));
					hookRegistrado = true;
					}
			}
			return con;

		} catch (IOException | ClassNotFoundException | SQLException ex) {
			throw new RuntimeException("Error al crear la conexion: " + ex.getMessage(), ex);
		}
	}


	public static synchronized void cerrar() {

		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private static Properties cargarPropiedades() throws IOException {

		File archivo = new File(ARCHIVO);

		if (!archivo.exists()) {
			throw new IOException("No se encontro " + archivo.getAbsolutePath()
					+ ". Copia jdbc.properties.example a jdbc.properties en la raiz del proyecto y completalo.");
		}

		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(archivo)) {
			prop.load(input);
		}
		return prop;
	}

	private static String requerida(Properties prop, String clave) throws IOException {

		String valor = prop.getProperty(clave);

		if (valor == null || valor.isBlank()) {
			throw new IOException("Falta la propiedad '" + clave + "' en " + ARCHIVO);
		}
		return valor.trim();
	}
}