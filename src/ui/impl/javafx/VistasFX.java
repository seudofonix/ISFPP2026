/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga las pantallas FXML (src/ui/javafx/view) con FXMLLoader y les entrega el ContextoAplicacion a sus
 * controladores. FXMLLoader.load() construye los controles, enlaza los campos @FXML y llama a initialize().
 */
package ui.impl.javafx;

import java.io.IOException;
import java.io.UncheckedIOException;

import aplicacion.ContextoAplicacion;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class VistasFX {

	private static final String CARPETA = "/ui/javafx/view/";

	private VistasFX() {
	}

	/** Carga el FXML; cada controlador se crea con un constructor que recibe el ContextoAplicacion. */
	public static Parent cargar(String archivoFxml, ContextoAplicacion contexto) {

		try {

			FXMLLoader loader = new FXMLLoader(VistasFX.class.getResource(CARPETA + archivoFxml));
			loader.setControllerFactory(tipo -> crearControlador(tipo, contexto));

			return loader.load();

		} catch (IOException e) {

			throw new UncheckedIOException("No se pudo cargar la pantalla " + archivoFxml, e);
		}
	}

	/** Reemplaza la escena de la ventana por la pantalla indicada. */
	public static void mostrar(Stage escenario, String archivoFxml, String titulo, ContextoAplicacion contexto) {

		Parent raiz = cargar(archivoFxml, contexto);

		escenario.setTitle(titulo);
		escenario.setScene(new Scene(raiz));
		escenario.sizeToScene();
		escenario.centerOnScreen();
		escenario.show();
	}

	private static Object crearControlador(Class<?> tipo, ContextoAplicacion contexto) {

		try {

			return tipo.getConstructor(ContextoAplicacion.class).newInstance(contexto);

		} catch (ReflectiveOperationException e) {

			throw new IllegalStateException("No se pudo crear el controlador " + tipo.getName(), e);
		}
	}
}
