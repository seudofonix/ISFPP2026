/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelConductor.fxml. Muestra los datos del conductor y la pestana "Tablas" con las
 * subpestanas de vehiculos, usuarios, servicios y viajes, para poder corroborar los datos cargados (desde la
 * base o desde los archivos, segun config.properties).
 */
package ui.impl.javafx.controller;

import java.time.format.DateTimeFormatter;

import java.util.List;

import aplicacion.ContextoAplicacion;
import idioma.Mensajes;
import ui.Formato;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import servicio.Servicio;
import ui.impl.javafx.VistasFX;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.RegistroViaje;
import viajes.Viaje;

public class PanelConductorController {

	private final ContextoAplicacion contexto;

	@FXML private Label lblConductor;
	@FXML private Label lblDetalle;
	@FXML private Button btnCerrarSesion;

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

	@FXML private TableView<Viaje> tablaViajes;
	@FXML private TableColumn<Viaje, String> colViaId;
	@FXML private TableColumn<Viaje, String> colViaCliente;
	@FXML private TableColumn<Viaje, String> colViaConductor;
	@FXML private TableColumn<Viaje, String> colViaServicio;
	@FXML private TableColumn<Viaje, String> colViaEstado;
	@FXML private TableView<Formato.FilaRegistro> tablaRegistro;
	@FXML private Label lblResumenRegistro;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegId;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegCliente;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegServicio;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegOrigen;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegDestino;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegEstado;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegFecha;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegCalificacion;
	@FXML private TableColumn<Formato.FilaRegistro, String> colRegComentario;
	@FXML private TableColumn<Viaje, String> colViaHora;

	@FXML private TableView<Servicio> tablaServicios;
	@FXML private TableColumn<Servicio, String> colSerNombre;
	@FXML private TableColumn<Servicio, Number> colSerBase;
	@FXML private TableColumn<Servicio, Number> colSerKm;
	@FXML private TableColumn<Servicio, Number> colSerMinuto;
	@FXML private TableColumn<Servicio, String> colSerVehiculo;
	@FXML private TableColumn<Servicio, String> colSerCategoria;
	@FXML private TableColumn<Servicio, String> colSerServicio;

	public PanelConductorController(ContextoAplicacion contexto) {

		this.contexto = contexto;
	}

	@FXML
	private void initialize() {

		Usuario conductor = contexto.getUsuarioLogueado();

		lblConductor.setText(Mensajes.get("conductor.titulo", conductor.getNombre(), conductor.getEmail()));
		lblDetalle.setText(Mensajes.get("conductor.detalle", conductor.getConductor().getLicenciaConducir(),
				conductor.getConductor().getEstadoConductor(),
				String.join(", ", conductor.getConductor().getVehiculos().keySet()), contexto.getOrigen()));

		configurarColumnas();

		tablaVehiculos.setItems(FXCollections.observableArrayList(contexto.getVehiculos()));
		tablaUsuarios.setItems(FXCollections.observableArrayList(contexto.getUsuarios()));
		tablaServicios.setItems(FXCollections.observableArrayList(contexto.getServicios()));
		handleActualizarViajes();
		handleActualizarRegistro();
	}

	/** Registro de los viajes hechos por este conductor (es la primera pestana del panel). */
	@FXML
	private void handleActualizarRegistro() {

		List<Viaje> viajes = contexto.getUsuarioLogueado().getConductor().getViajes();

		tablaRegistro.setItems(FXCollections.observableArrayList(Formato.registroDeConductor(contexto, viajes)));

		double promedio = Formato.promedio(viajes);
		lblResumenRegistro.setText(Mensajes.get("conductor.resumen", viajes.size(),
				promedio < 0 ? "-" : String.format(java.util.Locale.ROOT, "%.2f", promedio)));
	}

	/** Vuelve a leer los viajes que hay en memoria (los de todos los clientes). */
	@FXML
	private void handleActualizarViajes() {

		tablaViajes.setItems(FXCollections.observableArrayList(contexto.getUsuarios().stream()
				.filter(u -> u.getCliente() != null).flatMap(u -> u.getCliente().getViajes().stream()).toList()));
	}

	@FXML
	private void handleCerrarSesion() {

		contexto.cerrarSesion();

		VistasFX.mostrar((Stage) btnCerrarSesion.getScene().getWindow(), "Login.fxml", Mensajes.get("ventana.login"),
				contexto);
	}

	private void configurarRegistro() {

		colRegId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id()));
		colRegCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cliente()));
		colRegServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().servicio()));
		colRegOrigen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().origen()));
		colRegDestino.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().destino()));
		colRegEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado()));
		colRegFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha()));
		colRegCalificacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().calificacion()));
		colRegComentario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().comentario()));
		tablaRegistro.setPlaceholder(new Label(Mensajes.get("conductor.sinViajes")));
	}

	private void configurarColumnas() {

		DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm:ss");
		configurarRegistro();
		colViaId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId().toString().substring(0, 8)));
		colViaCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente().getNombre()));
		colViaConductor.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getConductor() == null ? "-" : c.getValue().getConductor().getNombre()));
		colViaServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getServicio().getNombre()));
		colViaEstado.setCellValueFactory(c -> new SimpleStringProperty(Mensajes.get("estado." + c.getValue().estadoActual().name())));
		colViaHora.setCellValueFactory(c -> {
			var registros = c.getValue().getRegistroViaje();
			RegistroViaje ultimo = registros.isEmpty() ? null : registros.get(registros.size() - 1);
			return new SimpleStringProperty(ultimo == null ? "-" : ultimo.getFechaHora().format(hora));
		});

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
