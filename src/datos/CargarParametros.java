/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga desde config.properties la configuracion de la aplicacion: origen de los datos (bd | archivo), rutas
 * de los archivos de texto, area de simulacion, velocidad media, motor de rutas y datos del mapa. Las claves
 * opcionales tienen un valor por defecto (el caso de Puerto Madryn), asi la configuracion geografica vive
 * en este archivo y no dentro de los controladores ni del JavaScript.
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

	// Area de simulacion y estimacion de tiempos
	private static double latitud1 = -42.832;
	private static double latitud2 = -42.704;
	private static double longitud1 = -65.126;
	private static double longitud2 = -65.005;
	private static double velocidadMediaKmh = 40.0;
	private static double toleranciaAjusteMetros = 200.0;
	private static double segundosTramo = 4.0;
	private static int pasosTramo = 10;
	private static double esperaMaximaSegundos = 60.0;
	private static int capacidadBandeja = 50;
	private static int maximoConductores = 3;
	// Motor de rutas
	private static String rutasMotor = "graphhopper";
	private static String rutasOsm = "mapas/puerto_madryn_ampliado.osm";
	private static String rutasCache = "mapas/graphhopper-cache-madryn-ampliado";

	// Mapa
	private static String ciudadNombre = "Puerto Madryn";
	private static double mapaCentroLatitud = -42.785;
	private static double mapaCentroLongitud = -65.070;
	private static int mapaZoom = 12;

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
		}

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
		toleranciaAjusteMetros = numero(properties, "ajuste.tolerancia.metros", 200.0);

		rutasMotor = texto(properties, "rutas.motor", "graphhopper").toLowerCase();
		rutasOsm = texto(properties, "rutas.osm", "mapas/puerto_madryn_ampliado.osm");
		rutasCache = texto(properties, "rutas.cache", "mapas/graphhopper-cache-madryn-ampliado");

		ciudadNombre = texto(properties, "ciudad.nombre", "Puerto Madryn");
		mapaCentroLatitud = numero(properties, "mapa.centro.latitud", -42.785);
		mapaCentroLongitud = numero(properties, "mapa.centro.longitud", -65.070);
		mapaZoom = (int) numero(properties, "mapa.zoom", 12);
		if (segundosTramo <= 0 || pasosTramo <= 0 || esperaMaximaSegundos <= 0
		        || capacidadBandeja <= 0 || maximoConductores <= 0) {
		    throw new IOException("Los parametros de simulacion de config.properties deben ser mayores que 0.");
		}
		if (velocidadMediaKmh <= 0) {

			throw new IOException("velocidad.media.kmh debe ser mayor que 0 en config.properties");
		}

		if (toleranciaAjusteMetros <= 0) {

			throw new IOException("ajuste.tolerancia.metros debe ser mayor que 0 en config.properties");
		}
	}

	private static int entero(Properties properties, String string, int i) {
		// TODO Auto-generated method stub
		return 0;
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

	private static String texto(Properties properties, String clave, String defecto) {

		String valor = properties.getProperty(clave);

		return valor == null || valor.isBlank() ? defecto : valor.trim();
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

	/** Distancia maxima para ajustar un punto a la calle mas proxima. */
	public static double getToleranciaAjusteMetros() {

		return toleranciaAjusteMetros;
	}

	public static String getRutasMotor() {

		return rutasMotor;
	}

	public static String getRutasOsm() {

		return rutasOsm;
	}

	public static String getRutasCache() {

		return rutasCache;
	}

	public static String getCiudadNombre() {

		return ciudadNombre;
	}

	public static double getMapaCentroLatitud() {

		return mapaCentroLatitud;
	}

	public static double getMapaCentroLongitud() {

		return mapaCentroLongitud;
	}

	public static int getMapaZoom() {

		return mapaZoom;
	}
	public static double getSegundosTramo() { return segundosTramo; }
	public static int getPasosTramo() { return pasosTramo; }
	public static double getEsperaMaximaSegundos() { return esperaMaximaSegundos; }
	public static int getCapacidadBandeja() { return capacidadBandeja; }
	public static int getMaximoConductores() { return maximoConductores; }
}
