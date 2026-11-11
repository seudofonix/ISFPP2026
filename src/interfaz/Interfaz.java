/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define el punto de partida para la interfaz de interacción con la aplicación (JavaFX). Crea el
 * Modelo y el Navegador y abre la pantalla de ingreso, donde se elige si se entra como usuario o
 * como conductor.
 */
package interfaz;

import controlador.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;
import modelo.Modelo;

public class Interfaz extends Application {

	private Modelo modelo;

	@Override
	public void start(Stage escenario) {

		modelo = new Modelo();
		Navegador navegador = new Navegador(escenario, modelo);

		navegador.mostrarLogin();
		escenario.show();
	}

	@Override
	public void stop() {

		if (modelo != null) {

			modelo.detener(); // cierra los hilos de la simulacion antes de salir
		}
	}
}
