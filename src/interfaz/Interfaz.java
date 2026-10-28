/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define el punto de partida para la interfaz de interacción con la aplicación (JavaFX). Arma el trio
 * Modelo - Vista - Controlador y muestra la ventana principal.
 */
package interfaz;

import java.io.IOException;
import controlador.Controlador;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Modelo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


public class Interfaz extends Application {

	@Override
	public void start(Stage escenario) throws IOException {

		Modelo modelo = new Modelo();

		FXMLLoader loader = new FXMLLoader(Interfaz.class.getResource("/vista/Vista.fxml"));
		loader.setControllerFactory(tipo -> new Controlador(modelo));
		Parent raiz = loader.load();

		escenario.setTitle("Movilidad Digital - ISFPP 2026");
		escenario.setScene(new Scene(raiz));
		escenario.show();
	}
}
