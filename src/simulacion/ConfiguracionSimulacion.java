/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Parametros de la simulacion de viajes. Se cargan desde config.properties para no dejar valores
 * fijos en el codigo.
 */
package simulacion;

/**
 * @param segundosTramo           duracion real de cada tramo del viaje
 * @param pasosTramo              cantidad de pasos en que se divide cada tramo
 * @param esperaMaximaSegundos    tiempo maximo que un viaje puede esperar a que un conductor lo acepte
 * @param capacidadBandeja        capacidad de la bandeja de cada conductor simulado
 * @param maximoConductores       cantidad maxima de conductores simulados
 * @param duracionSegundos        duracion total de la simulacion; 0 = sin limite
 * @param cantidadViajes          cantidad de viajes que genera el productor; 0 = sin limite
 * @param intervaloViajesSegundos tiempo entre viajes generados por el productor
 * @param productorAutomatico     indica si el productor genera viajes sin intervencion de la UI
 */
public record ConfiguracionSimulacion(double segundosTramo, int pasosTramo, double esperaMaximaSegundos,
		int capacidadBandeja, int maximoConductores, double duracionSegundos, int cantidadViajes,
		double intervaloViajesSegundos, boolean productorAutomatico) {

	public ConfiguracionSimulacion {

		if (segundosTramo <= 0 || pasosTramo <= 0 || esperaMaximaSegundos <= 0 || capacidadBandeja <= 0
				|| maximoConductores <= 0) {

			throw new IllegalArgumentException("Los parametros de la simulacion deben ser mayores que 0.");
		}

		if (duracionSegundos < 0 || cantidadViajes < 0 || intervaloViajesSegundos <= 0) {

			throw new IllegalArgumentException(
					"La duracion y la cantidad de viajes no pueden ser negativas, y el intervalo debe ser mayor que 0.");
		}
	}

	/**
	 * Constructor compatible con las pruebas y usos anteriores: sin productor automatico, sin
	 * duracion ni cantidad maxima, y con tres conductores como maximo.
	 */
	public ConfiguracionSimulacion(double segundosTramo, int pasosTramo, double esperaMaximaSegundos,
			int capacidadBandeja) {

		this(segundosTramo, pasosTramo, esperaMaximaSegundos, capacidadBandeja, 3, 0.0, 0, 5.0, false);
	}
}
