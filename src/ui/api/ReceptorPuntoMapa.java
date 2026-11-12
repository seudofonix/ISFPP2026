/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Quien recibe los clics del mapa: el tipo de punto ("origen" o "destino") y la ubicacion elegida.
 */
package ui.api;

import ubicacion.Ubicacion;

@FunctionalInterface
public interface ReceptorPuntoMapa {

	void recibir(String tipo, Ubicacion ubicacion);
}
