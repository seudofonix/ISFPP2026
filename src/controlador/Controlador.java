/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR del patron MVC. Enlaza el Modelo con la Vista: conecta las listas observables a las
 * tablas y atiende los eventos del usuario.
 */
package controlador;

import java.io.IOException;

import javafx.scene.control.Alert;
import modelo.Modelo;
import servicio.Servicio;
import vista.Vista;

public class Controlador {

	private final Modelo modelo;
	private final Vista vista;

	public Controlador(Modelo modelo, Vista vista) {

		this.modelo = modelo;
		this.vista = vista;

		enlazarModeloConVista();
		enlazarEventos();
		recargar();
	}

	private void enlazarModeloConVista() {

		vista.getTablaVehiculos().setItems(modelo.getVehiculos());
		vista.getTablaUsuarios().setItems(modelo.getUsuarios());
		vista.getTablaServicios().setItems(modelo.getServicios());
		vista.getComboServicio().setItems(modelo.getServicios());
	}

	private void enlazarEventos() {

		vista.getBotonRecargar().setOnAction(e -> recargar());
		vista.getBotonCalcular().setOnAction(e -> calcularCosto());
	}

	private void recargar() {

		try {

			modelo.cargar();
			vista.getEtiquetaEstado().setText("Cargados: " + modelo.getVehiculos().size() + " vehiculos, "
					+ modelo.getUsuarios().size() + " usuarios, " + modelo.getServicios().size() + " servicios");

		} catch (IOException | RuntimeException ex) {

			vista.getEtiquetaEstado().setText("Error al cargar los datos");
			mostrarError("No se pudieron cargar los datos", ex.getMessage());
		}
	}

	private void calcularCosto() {

		Servicio servicio = vista.getComboServicio().getValue();

		if (servicio == null) {

			mostrarError("Falta el servicio", "Elegi un servicio de la lista.");
			return;
		}

		try {

			double km = Double.parseDouble(vista.getCampoDistancia().getText().trim().replace(',', '.'));
			double min = Double.parseDouble(vista.getCampoDuracion().getText().trim().replace(',', '.'));

			if (km < 0 || min < 0) {

				throw new NumberFormatException();
			}

			vista.getEtiquetaCosto().setText(String.format("$ %.2f", modelo.calcularCosto(servicio, km, min)));

		} catch (NumberFormatException ex) {

			mostrarError("Datos invalidos", "Distancia y duracion deben ser numeros mayores o iguales a 0.");
		}
	}

	private void mostrarError(String titulo, String mensaje) {

		Alert alerta = new Alert(Alert.AlertType.ERROR);
		alerta.setTitle(titulo);
		alerta.setHeaderText(titulo);
		alerta.setContentText(mensaje);
		alerta.showAndWait();
	}
}
