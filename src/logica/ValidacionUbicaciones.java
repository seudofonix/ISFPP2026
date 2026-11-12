/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Reglas para aceptar coordenadas: que sean numeros validos, que esten dentro del area de simulacion
 * configurada y que se puedan ajustar a una calle transitable. Una marca en el mapa no significa que la
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
	 * Convierte los textos de latitud y longitud (acepta coma decimal) en una Ubicacion.
	 *
	 * @param nombre "Origen" o "Destino", solo para el mensaje de error
	 * @throws IllegalArgumentException si no son numeros o estan fuera de rango geografico
	 */
	public static Ubicacion validarCoordenadas(String latitud, String longitud, String nombre) {

		double lat;
		double lon;

		try {

			lat = Double.parseDouble(latitud.trim().replace(',', '.'));
			lon = Double.parseDouble(longitud.trim().replace(',', '.'));

		} catch (NumberFormatException | NullPointerException e) {

			throw new IllegalArgumentException("Latitud y longitud del " + nombre.toLowerCase()
					+ " deben ser numeros (ej: -42.77).");
		}

		if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180) {

			throw new IllegalArgumentException("Las coordenadas del " + nombre.toLowerCase() + " no son validas.");
		}

		return new Ubicacion(lat, lon);
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
