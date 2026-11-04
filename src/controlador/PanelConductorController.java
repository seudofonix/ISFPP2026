/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelConductor.fxml. Muestra los datos del conductor y la pestana "Tablas" con las
 * subpestanas de vehiculos, usuarios y servicios.
 */
package controlador;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import modelo.Modelo;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public class PanelConductorController {

	private final Modelo modelo;
	private final Navegador navegador;

	@FXML private Label etiquetaConductor;
	@FXML private Label etiquetaDetalle;

	@FXML private TableView<Vehiculo> tablaVehiculos;
	@FXML private TableColumn<Vehiculo, String> colVehPatente;
	@FXML private TableColumn<Vehiculo, String> colVehModelo;
	@FXML private TableColumn<Vehiculo, Number> colVehCapacidad;
	@FXML private TableColumn<Vehiculo, String> colVehTipo;
	@FXML private TableColumn<Vehiculo, String> colVehCategoria;
	@FXML private TableColumn<Vehiculo, String> colVehServicios;

	@FXML private TableView<Usuario> tablaUsuarios;
	@FXML private TableColumn<Usuario, String> colUsuNombre;
	@FXML private TableColumn<Usuario, String> colUsuTelefono;
	@FXML private TableColumn<Usuario, String> colUsuEmail;
	@FXML private TableColumn<Usuario, String> colUsuRol;
	@FXML private TableColumn<Usuario, String> colUsuLicencia;
	@FXML private TableColumn<Usuario, String> colUsuVehiculos;

	@FXML private TableView<Servicio> tablaServicios;
	@FXML private TableColumn<Servicio, String> colSerNombre;
	@FXML private TableColumn<Servicio, Number> colSerBase;
	@FXML private TableColumn<Servicio, Number> colSerKm;
	@FXML private TableColumn<Servicio, Number> colSerMinuto;
	@FXML private TableColumn<Servicio, String> colSerVehiculo;
	@FXML private TableColumn<Servicio, String> colSerCategoria;
	@FXML private TableColumn<Servicio, String> colSerServicio;

	public PanelConductorController(Modelo modelo, Navegador navegador) {

		this.modelo = modelo;
		this.navegador = navegador;
	}

	@FXML
	private void initialize() {

		Usuario conductor = modelo.getUsuarioActual();

		etiquetaConductor.setText("Conductor: " + conductor.getNombre() + " (" + conductor.getEmail() + ")");
		etiquetaDetalle.setText("Licencia: " + conductor.getConductor().getLicenciaConducir() + "  |  Estado: "
				+ conductor.getConductor().getEstadoConductor() + "  |  Vehiculos: "
				+ String.join(", ", conductor.getConductor().getVehiculos().keySet()));

		configurarColumnas();

		tablaVehiculos.setItems(modelo.getVehiculos());
		tablaUsuarios.setItems(modelo.getUsuarios());
		tablaServicios.setItems(modelo.getServicios());
	}

	@FXML
	private void cerrarSesion() {

		modelo.cerrarSesion();
		navegador.mostrarLogin();
	}

	private void configurarColumnas() {

		colVehPatente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatente()));
		colVehModelo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelo()));
		colVehCapacidad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidadPasajeros()));
		colVehTipo.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoVehiculo())));
		colVehCategoria.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getCategoriaVehiculo())));
		colVehServicios.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoServicio().toString()));

		colUsuNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
		colUsuTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
		colUsuEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
		colUsuRol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRolActivo())));
		colUsuLicencia.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getConductor() == null ? "-" : c.getValue().getConductor().getLicenciaConducir()));
		colUsuVehiculos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConductor() == null ? "-"
				: String.join(", ", c.getValue().getConductor().getVehiculos().keySet())));

		colSerNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
		colSerBase.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTarifaBase()));
		colSerKm.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioKm()));
		colSerMinuto.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioMinuto()));
		colSerVehiculo.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoVehiculo())));
		colSerCategoria.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getCategoriaVehiculo())));
		colSerServicio.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoServicio())));
	}
}
