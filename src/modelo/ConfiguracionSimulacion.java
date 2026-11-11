/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Parametros de la simulacion de viajes. Se cargan desde config.properties para no dejar valores
 * fijos en el codigo.
 */
package modelo;

/**
 * @param segundosTramo        duracion real de cada tramo del viaje
 * @param pasosTramo           cantidad de pasos en que se divide cada tramo
 * @param esperaMaximaSegundos tiempo maximo que un viaje puede esperar a que un conductor lo acepte
 * @param capacidadBandeja     capacidad de la bandeja de cada conductor simulado
 */
public record ConfiguracionSimulacion(double segundosTramo, int pasosTramo, double esperaMaximaSegundos,
		int capacidadBandeja) {

	public ConfiguracionSimulacion {

		if (segundosTramo <= 0 || pasosTramo <= 0 || esperaMaximaSegundos <= 0 || capacidadBandeja <= 0) {

			throw new IllegalArgumentException("Los parametros de la simulacion deben ser mayores que 0.");
		}
	}
}
