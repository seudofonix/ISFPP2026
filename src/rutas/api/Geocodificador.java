/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Contrato para mostrarle al usuario donde esta un punto del mapa: en lugar de latitud y longitud se informa
 * el nombre de la calle y la altura. La interfaz no decide de donde salen los datos (OpenStreetMap, un
 * servicio web, etc.).
 */
package rutas.api;

import ubicacion.Ubicacion;

public interface Geocodificador {

	/**
	 * Direccion legible de un punto (por ejemplo "Avenida Roca al 115" o "Lavalle 700").
	 *
	 * @return el texto a mostrar; nunca es null ni vacio
	 */
	String direccion(Ubicacion ubicacion);
}
