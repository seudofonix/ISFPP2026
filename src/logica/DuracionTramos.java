/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Tiempos simulados de los tramos de un viaje con la MISMA velocidad en ambos. La velocidad de referencia es la
 * del tramo origen -> destino: recorrerlo dura simulacion.segundos.tramo. El tramo conductor -> origen dura lo
 * que corresponde a su distancia a esa misma velocidad.
 */
package logica;

public final class DuracionTramos {

	/** Piso para que un conductor que ya esta en el origen no genere una espera de 0 segundos. */
	public static final double MINIMO_SEGUNDOS = 0.2;

	private DuracionTramos() {
	}

	/**
	 * @param kmRecogida        distancia por calle del conductor al origen
	 * @param kmTraslado        distancia por calle del origen al destino
	 * @param segundosTraslado lo que dura recorrer el traslado (velocidad de referencia)
	 * @return segundos que dura la recogida a la misma velocidad; si no hay distancia de referencia, igual al traslado
	 */
	public static double recogida(double kmRecogida, double kmTraslado, double segundosTraslado) {

		if (kmTraslado <= 0) {

			return segundosTraslado;
		}

		return Math.max(MINIMO_SEGUNDOS, segundosTraslado * kmRecogida / kmTraslado);
	}
}
