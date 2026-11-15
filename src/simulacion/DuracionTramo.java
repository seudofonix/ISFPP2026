/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Calcula cuanto dura un tramo de un viaje simulado. Permite que la simulacion consulte tiempos que
 * dependen del viaje (por ejemplo, la recogida dura segun la distancia del conductor al origen) sin
 * conocer el motor de rutas ni la configuracion de la aplicacion.
 */
package simulacion;

import viajes.EstadoViaje;
import viajes.Viaje;

@FunctionalInterface
public interface DuracionTramo {

	/**
	 * @param viaje viaje que se esta simulando
	 * @param tramo estado que se esta recorriendo (ACEPTADO = recogida, INICIADO = traslado)
	 * @return segundos reales que dura ese tramo
	 */
	double segundos(Viaje viaje, EstadoViaje tramo);
}
