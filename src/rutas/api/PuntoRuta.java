/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Punto geografico (latitud, longitud) de la polilinea de una ruta. Se conserva el orden del recorrido.
 */
package rutas.api;

public record PuntoRuta(double latitud, double longitud) {

	public PuntoRuta {

		if (!Double.isFinite(latitud) || !Double.isFinite(longitud)) {

			throw new IllegalArgumentException("Las coordenadas de un punto de ruta deben ser numeros finitos.");
		}
	}
}
