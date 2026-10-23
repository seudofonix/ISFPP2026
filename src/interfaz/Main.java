/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Lanzador de la aplicacion. Se separa de Interfaz (que extiende Application) para que tambien se
 * pueda ejecutar fuera de Maven sin el error "JavaFX runtime components are missing".
 */
package interfaz;

import javafx.application.Application;

public class Main {

	public static void main(String[] args) {

		Application.launch(Interfaz.class, args);
	}
}
