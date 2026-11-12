/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Punto de entrada de la aplicacion. Crea el ContextoAplicacion (datos, motor vial, escenario) y se lo entrega
 * a la interfaz de usuario. Al volver de la interfaz cierra el contexto (hilos y grafo de rutas).
 */
package aplicacion;

import ui.api.UIEngine;
import ui.impl.javafx.JavaFXUserInterface;

public class AplicacionConsultas {

	public static void main(String[] args) {

		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

		ContextoAplicacion contexto;

		try {

			contexto = ContextoAplicacion.crear();

		} catch (Exception e) {

			System.err.println("No se pudo iniciar la aplicacion: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
			return;
		}

		try (contexto) {

			UIEngine interfaz = new JavaFXUserInterface();
			interfaz.iniciar(contexto);
		}
	}
}
