/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR del patron MVC. La Vista es el archivo vista/Vista.fxml (se edita con Scene Builder);
 * los campos @FXML se enlazan por fx:id y los metodos @FXML por onAction. El Modelo se inyecta por
 * constructor desde Interfaz.
 */
package controlador;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Modelo;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public class Controlador {

	private final Modelo modelo;

	// ---- Barra superior ----
	@FXML private Button botonRecargar;
	@FXML private Label etiquetaEstado;

	// ---- Vehiculos ----
	@FXML private TableView<Vehiculo> tablaVehiculos;
	@FXML private TableColumn<Vehiculo, String> colVehPatente;
	@FXML private TableColumn<Vehiculo, String> colVehModelo;
	@FXML private TableColumn<Vehiculo, Number> colVehCapacidad;
	@FXML private TableColumn<Vehiculo, String> colVehTipo;
	@FXML private TableColumn<Vehiculo, String> colVehCategoria;
	@FXML private TableColumn<Vehiculo, String> colVehServicios;

	// ---- Usuarios ----
	@FXML private TableView<Usuario> tablaUsuarios;
	@FXML private TableColumn<Usuario, String> colUsuNombre;
	@FXML private TableColumn<Usuario, String> colUsuTelefono;
	@FXML private TableColumn<Usuario, String> colUsuEmail;
	@FXML private TableColumn<Usuario, String> colUsuRol;
	@FXML private TableColumn<Usuario, String> colUsuLicencia;
	@FXML private TableColumn<Usuario, String> colUsuVehiculos;

	// ---- Servicios ----
	@FXML private TableView<Servicio> tablaServicios;
	@FXML private TableColumn<Servicio, String> colSerNombre;
	@FXML private TableColumn<Servicio, Number> colSerBase;
	@FXML private TableColumn<Servicio, Number> colSerKm;
	@FXML private TableColumn<Servicio, Number> colSerMinuto;
	@FXML private TableColumn<Servicio, String> colSerVehiculo;
	@FXML private TableColumn<Servicio, String> colSerCategoria;
	@FXML private TableColumn<Servicio, String> colSerServicio;

	// ---- Cotizador ----
	@FXML private ComboBox<Servicio> comboServicio;
	@FXML private TextField campoDistancia;
	@FXML private TextField campoDuracion;
	@FXML private Button botonCalcular;
	@FXML private Label etiquetaCosto;

	public Controlador(Modelo modelo) {

		this.modelo = modelo;
	}

	/** Lo invoca FXMLLoader una vez inyectados todos los campos @FXML. */
	@FXML
	private void initialize() {

		configurarColumnas();

		tablaVehiculos.setItems(modelo.getVehiculos());
		tablaUsuarios.setItems(modelo.getUsuarios());
		tablaServicios.setItems(modelo.getServicios());
		comboServicio.setItems(modelo.getServicios());

		recargar();
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

	@FXML
	private void recargar() {

		try {

			modelo.cargar();
			etiquetaEstado.setText("Cargados: " + modelo.getVehiculos().size() + " vehiculos, "
					+ modelo.getUsuarios().size() + " usuarios, " + modelo.getServicios().size() + " servicios");

		} catch (IOException | RuntimeException ex) {

			etiquetaEstado.setText("Error al cargar los datos");
			mostrarError("No se pudieron cargar los datos", ex.getMessage());
		}
	}

	@FXML
	private void calcularCosto() {

		Servicio servicio = comboServicio.getValue();

		if (servicio == null) {

			mostrarError("Falta el servicio", "Elegi un servicio de la lista.");
			return;
		}

		try {

			double km = Double.parseDouble(campoDistancia.getText().trim().replace(',', '.'));
			double min = Double.parseDouble(campoDuracion.getText().trim().replace(',', '.'));

			if (km < 0 || min < 0) {

				throw new NumberFormatException();
			}

			etiquetaCosto.setText(String.format("$ %.2f", modelo.calcularCosto(servicio, km, min)));

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
