/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Dialogos de aviso compartidos por los controladores.
 */
package controlador;

import javafx.scene.control.Alert;

final class Alertas {

	private Alertas() {
	}

	static void error(String titulo, String mensaje) {

		mostrar(Alert.AlertType.ERROR, titulo, mensaje);
	}

	static void info(String titulo, String mensaje) {

		mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
	}

	private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {

		Alert alerta = new Alert(tipo);
		alerta.setTitle(titulo);
		alerta.setHeaderText(titulo);
		alerta.setContentText(mensaje);
		alerta.showAndWait();
	}
}
