/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Resultado neutral (sin tipos de GraphHopper) de calcular un tramo vial: distancia, duracion estimada y
 * geometria ordenada del recorrido.
 */
package rutas.api;

import java.util.List;

public record RutaCalculada(double distanciaMetros, long duracionMilisegundos, List<PuntoRuta> geometria) {

	public RutaCalculada {

		if (!Double.isFinite(distanciaMetros) || distanciaMetros < 0) {

			throw new IllegalArgumentException("La distancia de la ruta no puede ser negativa.");
		}

		if (duracionMilisegundos < 0) {

			throw new IllegalArgumentException("La duracion de la ruta no puede ser negativa.");
		}

		if (geometria == null || geometria.size() < 2) {

			throw new IllegalArgumentException("Una ruta necesita al menos dos puntos.");
		}

		geometria = List.copyOf(geometria);
	}

	public double distanciaKm() {

		return distanciaMetros / 1000.0;
	}

	public double duracionMinutos() {

		return duracionMilisegundos / 60000.0;
	}
}
