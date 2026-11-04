/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga desde config.properties las rutas de los archivos de datos, el origen de los datos
 * (archivo | bd) y los parametros de la simulacion (area geografica y velocidad media). Las claves
 * de la simulacion son opcionales: si faltan se usan los valores de Puerto Madryn.
 */
package datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {

	private static String archivoUsuarios;
	private static String archivoServicios;
	private static String archivoVehiculos;
	private static String origen = "archivo";

	private static double latitud1 = -42.832;
	private static double latitud2 = -42.704;
	private static double longitud1 = -65.126;
	private static double longitud2 = -65.005;
	private static double velocidadMediaKmh = 40.0;

	public static void parametros() throws IOException {

		Properties properties = new Properties();

		InputStream entrada = CargarParametros.class.getClassLoader().getResourceAsStream("config.properties");

		if (entrada == null) {

			File archivoConfig = new File("config.properties");

			if (!archivoConfig.exists()) {

				archivoConfig = new File("src/config.properties");
			}

			entrada = new FileInputStream(archivoConfig);
		}

		try (InputStream in = entrada) {

			properties.load(in);

			archivoUsuarios = properties.getProperty("usuario");
			archivoServicios = properties.getProperty("servicio");
			archivoVehiculos = properties.getProperty("vehiculo");
			origen = properties.getProperty("origen", "archivo").trim().toLowerCase();

			latitud1 = numero(properties, "latitud1", -42.832);
			latitud2 = numero(properties, "latitud2", -42.704);
			longitud1 = numero(properties, "longitud1", -65.126);
			longitud2 = numero(properties, "longitud2", -65.005);
			velocidadMediaKmh = numero(properties, "velocidad.media.kmh", 40.0);
		}

		if (velocidadMediaKmh <= 0) {

			throw new IOException("velocidad.media.kmh debe ser mayor que 0 en config.properties");
		}
	}

	private static double numero(Properties properties, String clave, double defecto) throws IOException {

		String valor = properties.getProperty(clave);

		if (valor == null || valor.isBlank()) {

			return defecto;
		}

		try {

			return Double.parseDouble(valor.trim());

		} catch (NumberFormatException e) {

			throw new IOException("Valor invalido para '" + clave + "' en config.properties: " + valor);
		}
	}

	public static String getArchivoUsuarios() {

		return archivoUsuarios;
	}

	public static String getArchivoServicios() {

		return archivoServicios;
	}

	public static String getArchivoVehiculos() {

		return archivoVehiculos;
	}

	/** Origen de los datos: "archivo" (por defecto) o "bd". */
	public static String getOrigen() {

		return origen;
	}

	public static double getLatitud1() {

		return latitud1;
	}

	public static double getLatitud2() {

		return latitud2;
	}

	public static double getLongitud1() {

		return longitud1;
	}

	public static double getLongitud2() {

		return longitud2;
	}

	/** Velocidad media usada para estimar la duracion de un viaje a partir de la distancia. */
	public static double getVelocidadMediaKmh() {

		return velocidadMediaKmh;
	}
}
