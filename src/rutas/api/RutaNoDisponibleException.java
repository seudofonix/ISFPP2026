/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Error propio del contrato de rutas: el motor no pudo encontrar un recorrido. No traslada excepciones
 * internas del proveedor.
 */
package rutas.api;

public class RutaNoDisponibleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RutaNoDisponibleException(String mensaje) {

		super(mensaje);
	}

	public RutaNoDisponibleException(String mensaje, Throwable causa) {

		super(mensaje, causa);
	}
}
