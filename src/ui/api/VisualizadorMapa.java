/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Contrato del componente cartografico que usa el controlador. Declara que se necesita del mapa sin crear un
 * WebView, cargar HTML ni depender de Leaflet u otro proveedor.
 */
package ui.api;

import rutas.api.RutasViaje;
import ubicacion.Ubicacion;

public interface VisualizadorMapa {

	/** Dibuja los dos tramos (acercamiento y traslado) y los marcadores de conductor, origen y destino. */
	void mostrar(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino);

	/** Elimina las polilineas de las rutas anteriores (no los marcadores de seleccion). */
	void limpiarRutas();

	/** Elimina los marcadores de origen y destino elegidos. */
	void limpiarSeleccion();

	/** Deja de esperar un clic en el mapa. */
	void cancelarSeleccion();

	/** Registra quien recibe los clics del mapa. */
	void configurarSeleccion(ReceptorPuntoMapa receptor);

	/** Pide al mapa que espere un clic para el punto indicado ("origen" o "destino"). */
	void activarSeleccion(String tipo);

	/** Dibuja el marcador definitivo de un punto ya validado. */
	void mostrarMarcadorSeleccion(String tipo, Ubicacion ubicacion);
}
