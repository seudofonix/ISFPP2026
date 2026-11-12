/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Implementacion JavaFX de UIEngine.
 */
package ui.impl.javafx;

import aplicacion.ContextoAplicacion;
import javafx.application.Application;
import ui.api.UIEngine;

public class JavaFXUserInterface implements UIEngine {

	@Override
	public void iniciar(ContextoAplicacion contexto) {

		MainApp.setContexto(contexto);
		Application.launch(MainApp.class);
	}
}
