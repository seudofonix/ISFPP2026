/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Cambia la pantalla de la ventana: carga cada FXML (disenado en Scene Builder) y le inyecta a su
 * controlador el Modelo y este Navegador.
 */
package controlador;

import java.io.IOException;
import java.io.UncheckedIOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Modelo;

public class Navegador {

	private static final String CARPETA_VISTAS = "/vista/";

	private final Stage escenario;
	private final Modelo modelo;

	public Navegador(Stage escenario, Modelo modelo) {

		this.escenario = escenario;
		this.modelo = modelo;
	}

	public void mostrarLogin() {

		cargar("Login.fxml", "Movilidad Digital - Ingreso");
	}

	public void mostrarPanelCliente() {

		cargar("PanelCliente.fxml", "Movilidad Digital - Usuario");
	}

	public void mostrarPanelConductor() {

		cargar("PanelConductor.fxml", "Movilidad Digital - Conductor");
	}

	private void cargar(String archivoFxml, String titulo) {

		try {

			FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(CARPETA_VISTAS + archivoFxml));
			loader.setControllerFactory(this::crearControlador);
			Parent raiz = loader.load();

			escenario.setTitle(titulo);
			escenario.setScene(new Scene(raiz));
			escenario.sizeToScene();
			escenario.centerOnScreen();

		} catch (IOException e) {

			throw new UncheckedIOException("No se pudo cargar la pantalla " + archivoFxml, e);
		}
	}

	private Object crearControlador(Class<?> tipo) {

		if (tipo == LoginController.class) {

			return new LoginController(modelo, this);
		}

		if (tipo == PanelClienteController.class) {

			return new PanelClienteController(modelo, this);
		}

		if (tipo == PanelConductorController.class) {

			return new PanelConductorController(modelo, this);
		}

		throw new IllegalArgumentException("Controlador desconocido: " + tipo.getName());
	}
}
