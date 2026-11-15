/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Punto de entrada de la aplicacion (raiz de composicion). Crea el ContextoAplicacion (datos, motor vial,
 * escenario), fija el idioma configurado y le pide a UIFactory la interfaz elegida en config.properties
 * (fx | consola). Vive fuera de los paquetes de capas para que 'aplicacion' no dependa de 'ui'. Al volver de la
 * interfaz cierra el contexto (hilos y grafo de rutas).
 */
package principal;

import aplicacion.ContextoAplicacion;
import datos.CargarParametros;
import idioma.Mensajes;
import ui.UIFactory;
import ui.api.UIEngine;

public class AplicacionConsultas {

	public static void main(String[] args) {

		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

		ContextoAplicacion contexto;
		UIEngine interfaz;

		try {

			contexto = ContextoAplicacion.crear();
			Mensajes.setIdioma(CargarParametros.getIdioma());
			interfaz = UIFactory.crear(CargarParametros.getUi());

		} catch (Exception e) {

			System.err.println(Mensajes.get("principal.errorInicio", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
			return;
		}

		try (contexto) {

			interfaz.iniciar(contexto);
		}
	}
}
