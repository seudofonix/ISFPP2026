/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Los dos tramos de un viaje, con sus metricas separadas: del conductor al origen (haciaOrigen,
 * acercamiento) y del origen al destino (haciaDestino, traslado).
 */
package rutas.api;

import java.util.Objects;

public record RutasViaje(RutaCalculada haciaOrigen, RutaCalculada haciaDestino) {

	public RutasViaje {

		Objects.requireNonNull(haciaOrigen, "haciaOrigen");
		Objects.requireNonNull(haciaDestino, "haciaDestino");
	}
}
