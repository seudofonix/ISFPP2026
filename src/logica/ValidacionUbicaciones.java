/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Reglas para aceptar un punto elegido en el mapa: que este dentro del area de simulacion configurada y que se
 * pueda ajustar a una calle transitable. Una marca en el mapa no significa que la
 * ubicacion sea valida para el motor de rutas.
 */
package logica;

import java.util.Locale;

import datos.CargarParametros;
import rutas.api.PlanificadorRutas;
import ubicacion.Ubicacion;

public final class ValidacionUbicaciones {

	private ValidacionUbicaciones() {
	}

	/**
	 * Comprueba que el punto este dentro del area de simulacion y lo ajusta a la calle transitable mas cercana.
	 *
	 * @return la ubicacion ajustada sobre la calle
	 * @throws IllegalArgumentException si esta fuera del area o no hay una calle suficientemente cerca
	 */
	public static Ubicacion validarYAjustar(Ubicacion candidata, PlanificadorRutas motor) {

		if (candidata == null) {

			throw new IllegalArgumentException("Falta la ubicacion.");
		}

		validarArea(candidata);

		double tolerancia = CargarParametros.getToleranciaAjusteMetros();

		return motor.ajustarACalle(candidata, tolerancia).orElseThrow(() -> new IllegalArgumentException(
				String.format(Locale.ROOT, "No hay una calle transitable a menos de %.0f m de ese punto.", tolerancia)));
	}

	/** @throws IllegalArgumentException si el punto cae fuera del rectangulo de simulacion */
	public static void validarArea(Ubicacion ubicacion) {

		double latMin = Math.min(CargarParametros.getLatitud1(), CargarParametros.getLatitud2());
		double latMax = Math.max(CargarParametros.getLatitud1(), CargarParametros.getLatitud2());
		double lonMin = Math.min(CargarParametros.getLongitud1(), CargarParametros.getLongitud2());
		double lonMax = Math.max(CargarParametros.getLongitud1(), CargarParametros.getLongitud2());

		boolean dentro = ubicacion.getLatitud() >= latMin && ubicacion.getLatitud() <= latMax
				&& ubicacion.getLongitud() >= lonMin && ubicacion.getLongitud() <= lonMax;

		if (!dentro) {

			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"El punto esta fuera del area de simulacion (latitud %.3f a %.3f, longitud %.3f a %.3f).", latMin,
					latMax, lonMin, lonMax));
		}
	}
}
