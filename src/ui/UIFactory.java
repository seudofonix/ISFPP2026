/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Factory de interfaces de usuario: segun la clave 'ui' de config.properties (fx | consola) entrega la
 * implementacion de UIEngine. Quien la usa solo conoce el contrato UIEngine; las pantallas JavaFX y la
 * consola no se conocen entre si. Para agregar otra interfaz: nueva implementacion de UIEngine y una rama en
 * crear().
 */
package ui;

import ui.api.UIEngine;
import ui.impl.consola.ConsolaUserInterface;
import ui.impl.javafx.JavaFXUserInterface;

public final class UIFactory {

	public static final String FX = "fx";
	public static final String CONSOLA = "consola";

	private UIFactory() {
	}

	/**
	 * @param tipo "fx" (JavaFX) o "consola"
	 * @throws IllegalArgumentException si el tipo no existe
	 */
	public static UIEngine crear(String tipo) {

		if (FX.equals(tipo)) {

			return new JavaFXUserInterface();
		}

		if (CONSOLA.equals(tipo)) {

			return new ConsolaUserInterface();
		}

		throw new IllegalArgumentException("Interfaz desconocida: '" + tipo + "'. Usar '" + FX + "' o '" + CONSOLA + "'.");
	}
}
