/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Aplicacion JavaFX: abre la pantalla de ingreso y, al cerrarse la ventana, detiene los hilos de simulacion.
 */
package ui.impl.javafx;

import aplicacion.ContextoAplicacion;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

	private static ContextoAplicacion contexto;

	static void setContexto(ContextoAplicacion nuevoContexto) {

		contexto = nuevoContexto;
	}

	@Override
	public void start(Stage escenario) {

		VistasFX.mostrar(escenario, "Login.fxml", "Movilidad Digital - Ingreso", contexto);
	}

	@Override
	public void stop() {

		if (contexto != null) {

			contexto.detener(); // cierra los hilos de los conductores antes de salir
		}
	}
}
