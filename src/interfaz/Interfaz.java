/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define el punto de partida para la interfaz de interacción con la aplicación (JavaFX). Arma el trio
 * Modelo - Vista - Controlador y muestra la ventana principal.
 */
package interfaz;

import controlador.Controlador;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Modelo;
import vista.Vista;

public class Interfaz extends Application {

	@Override
	public void start(Stage escenario) {

		Modelo modelo = new Modelo();
		Vista vista = new Vista();
		new Controlador(modelo, vista);

		escenario.setTitle("Movilidad Digital - ISFPP 2026");
		escenario.setScene(new Scene(vista.getRaiz(), 900, 520));
		escenario.show();
	}
}
