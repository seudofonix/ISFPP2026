/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Cotizacion comercial de un viaje antes de pedirlo: distancia en linea recta, duracion estimada con la velocidad
 * media configurada y costo segun el servicio. La usan todas las interfaces, asi el precio es el mismo en
 * JavaFX y en consola.
 */
package logica;

import datos.CargarParametros;
import idioma.Mensajes;
import modelo.Cotizacion;
import servicio.Servicio;
import ubicacion.Ubicacion;

public final class Cotizador {

	private Cotizador() {
	}

	/**
	 * @throws IllegalArgumentException si falta algun dato o el origen y el destino son el mismo punto
	 */
	public static Cotizacion cotizar(Servicio servicio, Ubicacion origen, Ubicacion destino) {

		if (servicio == null) {

			throw new IllegalArgumentException(Mensajes.get("error.faltaServicio"));
		}

		if (origen == null || destino == null) {

			throw new IllegalArgumentException(Mensajes.get("error.faltanPuntos"));
		}

		double km = origen.calcularDistancia(destino);

		if (km <= 0) {

			throw new IllegalArgumentException(Mensajes.get("error.mismoPunto"));
		}

		double minutos = km / CargarParametros.getVelocidadMediaKmh() * 60.0;

		return new Cotizacion(km, minutos, servicio.calcularCosto(km, minutos));
	}
}
