/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Objeto publico minimo que se expone a JavaScript como "javaBridge". Solo recibe el clic del mapa (tipo,
 * latitud, longitud) y lo pasa al controlador en el hilo de JavaFX. No expone reglas de negocio ni acceso a
 * datos.
 */
package ui.impl.javafx.map;

import javafx.application.Platform;
import ubicacion.Ubicacion;
import ui.api.ReceptorPuntoMapa;

public class PuenteSeleccionMapa {

	private final ReceptorPuntoMapa receptor;

	public PuenteSeleccionMapa(ReceptorPuntoMapa receptor) {

		this.receptor = receptor;
	}

	/** Lo invoca mapa.html cuando se hace clic y hay una seleccion activa. */
	public void seleccionarPunto(String tipo, double latitud, double longitud) {

		if (!"origen".equals(tipo) && !"destino".equals(tipo)) {

			return;
		}

		if (!Double.isFinite(latitud) || !Double.isFinite(longitud) || Math.abs(latitud) > 90
				|| Math.abs(longitud) > 180) {

			return;
		}

		Ubicacion ubicacion = new Ubicacion(latitud, longitud);

		Platform.runLater(() -> receptor.recibir(tipo, ubicacion));
	}
}
