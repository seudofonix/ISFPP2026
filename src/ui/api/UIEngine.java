/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Contrato de la interfaz de usuario: lo unico que necesita la aplicacion para arrancarla. Hoy solo existe la
 * implementacion JavaFX.
 */
package ui.api;

import aplicacion.ContextoAplicacion;

public interface UIEngine {

	/** Muestra la interfaz y devuelve el control cuando el usuario la cierra. */
	void iniciar(ContextoAplicacion contexto);
}
