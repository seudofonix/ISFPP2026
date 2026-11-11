/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga desde config.properties las rutas de los archivos de datos, el origen de los datos
 * (archivo | bd) y los parametros de la simulacion (area geografica, velocidad media, tiempos de
 * los viajes y cantidad de conductores). Las claves de la simulacion son opcionales: si faltan se
 * usan los valores por defecto del proyecto.
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

	private static double segundosTramo = 4.0;
	private static int pasosTramo = 10;
	private static double esperaMaximaSegundos = 60.0;
	private static int capacidadBandeja = 50;
	private static int maximoConductores = 3;

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
			origen = normalizarOrigen(properties.getProperty("origen", "archivo"));

			latitud1 = numero(properties, "latitud1", -42.832);
			latitud2 = numero(properties, "latitud2", -42.704);
			longitud1 = numero(properties, "longitud1", -65.126);
			longitud2 = numero(properties, "longitud2", -65.005);
			velocidadMediaKmh = numero(properties, "velocidad.media.kmh", 40.0);

			segundosTramo = numero(properties, "simulacion.segundos.tramo", 4.0);
			pasosTramo = entero(properties, "simulacion.pasos.tramo", 10);
			esperaMaximaSegundos = numero(properties, "simulacion.espera.maxima.segundos", 60.0);
			capacidadBandeja = entero(properties, "simulacion.capacidad.cola", 50);
			maximoConductores = entero(properties, "simulacion.maximo.conductores", 3);
		}

		if (velocidadMediaKmh <= 0) {

			throw new IOException("velocidad.media.kmh debe ser mayor que 0 en config.properties");
		}

		if (segundosTramo <= 0 || pasosTramo <= 0 || esperaMaximaSegundos <= 0 || capacidadBandeja <= 0
				|| maximoConductores <= 0) {

			throw new IOException("Los parametros de simulacion de config.properties deben ser mayores que 0.");
		}
	}

	/** Acepta "archivo" o "bd" (tambien "db"); cualquier otro valor es un error y no se usa otro origen en silencio. */
	static String normalizarOrigen(String valor) throws IOException {

		String limpio = valor.trim().toLowerCase();

		if (limpio.equals("db")) {

			return "bd";
		}

		if (!limpio.equals("bd") && !limpio.equals("archivo")) {

			throw new IOException("Valor invalido para 'origen' en config.properties: '" + valor.trim()
					+ "'. Usar 'bd' (base de datos) o 'archivo'.");
		}

		return limpio;
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

	private static int entero(Properties properties, String clave, int defecto) throws IOException {

		String valor = properties.getProperty(clave);

		if (valor == null || valor.isBlank()) {

			return defecto;
		}

		try {

			return Integer.parseInt(valor.trim());

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

	/** Duracion real de cada tramo de la simulacion. */
	public static double getSegundosTramo() {

		return segundosTramo;
	}

	/** Cantidad de pasos en que se divide cada tramo de la simulacion. */
	public static int getPasosTramo() {

		return pasosTramo;
	}

	/** Tiempo maximo que un viaje puede esperar a que un conductor lo acepte. */
	public static double getEsperaMaximaSegundos() {

		return esperaMaximaSegundos;
	}

	/** Capacidad de la bandeja de cada conductor simulado. */
	public static int getCapacidadBandeja() {

		return capacidadBandeja;
	}

	/** Cantidad maxima de conductores que se simulan a la vez. */
	public static int getMaximoConductores() {

		return maximoConductores;
	}
}
