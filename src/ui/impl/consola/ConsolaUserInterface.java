/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Implementacion de UIEngine para la consola. Arma la vista (entrada y salida estandar) y el controlador y deja
 * correr el dialogo hasta que el usuario sale. No usa JavaFX.
 */
package ui.impl.consola;

import aplicacion.ContextoAplicacion;
import ui.api.UIEngine;

public class ConsolaUserInterface implements UIEngine {

	@Override
	public void iniciar(ContextoAplicacion contexto) {

		new ControladorConsola(contexto, new VistaConsola(System.in, System.out)).ejecutar();
	}
}
