/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelCliente.fxml. El usuario elige servicio, origen y destino (a mano o aleatorios),
 * cotiza el viaje y lo reserva. Cualquier cambio en el formulario invalida la cotizacion anterior.
 * Origen y destino quedan guardados como Ubicacion en el Viaje, que es el dato que despues necesita el
 * calculo de la ruta.
 */
package controlador;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import modelo.Cotizacion;
import modelo.Modelo;
import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.Usuario;
import viajes.EstadoViaje;
import viajes.Viaje;

public class PanelClienteController {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final Modelo modelo;
	private final Navegador navegador;

	@FXML private Label etiquetaUsuario;
	@FXML private TabPane pestanias;

	// ---- Cotizar y reservar ----
	@FXML private ComboBox<Servicio> comboServicio;
	@FXML private TextField txtLatOrigen;
	@FXML private TextField txtLonOrigen;
	@FXML private TextField txtLatDestino;
	@FXML private TextField txtLonDestino;
	@FXML private Button botonCotizar;
	@FXML private Button botonReservar;
	@FXML private Label etiquetaDistancia;
	@FXML private Label etiquetaDuracion;
	@FXML private Label etiquetaCosto;

	// ---- Mis reservas ----
	@FXML private TableView<Viaje> tablaReservas;
	@FXML private TableColumn<Viaje, String> colResId;
	@FXML private TableColumn<Viaje, String> colResFecha;
	@FXML private TableColumn<Viaje, String> colResServicio;
	@FXML private TableColumn<Viaje, String> colResOrigen;
	@FXML private TableColumn<Viaje, String> colResDestino;
	@FXML private TableColumn<Viaje, String> colResDistancia;
	@FXML private TableColumn<Viaje, String> colResCosto;
	@FXML private TableColumn<Viaje, String> colResEstado;
	@FXML private Button botonCancelar;

	public PanelClienteController(Modelo modelo, Navegador navegador) {

		this.modelo = modelo;
		this.navegador = navegador;
	}

	@FXML
	private void initialize() {

		Usuario usuario = modelo.getUsuarioActual();
		etiquetaUsuario.setText("Usuario: " + usuario.getNombre() + " (" + usuario.getEmail() + ")");

		comboServicio.setItems(modelo.getServicios());
		comboServicio.setConverter(new StringConverter<>() {

			@Override
			public String toString(Servicio s) {
				return s == null ? ""
						: s.getNombre() + " (" + s.getTipoVehiculo() + ", " + s.getCategoriaVehiculo() + ", "
								+ s.getTipoServicio() + ")";
			}

			@Override
			public Servicio fromString(String texto) {
				return null;
			}
		});
		comboServicio.valueProperty().addListener((obs, a, b) -> invalidarCotizacion());

		for (TextField campo : new TextField[] { txtLatOrigen, txtLonOrigen, txtLatDestino, txtLonDestino }) {

			campo.textProperty().addListener((obs, a, b) -> invalidarCotizacion());
		}

		configurarTablaReservas();

		tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> actualizarBotonCancelar());
		modelo.getReservas().addListener((javafx.collections.ListChangeListener<Viaje>) c -> actualizarBotonCancelar());

		invalidarCotizacion();
		actualizarBotonCancelar();
		// Arranca los conductores simulados que van a tomar los viajes de este panel.
		modelo.iniciarSimulacion();
	}

	// ------------------------------------------------------------------ acciones del FXML

	@FXML
	private void generarOrigen() {

		completar(modelo.generarUbicacionAleatoria(), txtLatOrigen, txtLonOrigen);
	}

	@FXML
	private void generarDestino() {

		completar(modelo.generarUbicacionAleatoria(), txtLatDestino, txtLonDestino);
	}

	@FXML
	private void cotizar() {

		try {

			Cotizacion c = modelo.cotizar(comboServicio.getValue(), leerOrigen(), leerDestino());

			etiquetaDistancia.setText(String.format(Locale.ROOT, "%.2f km", c.distanciaKm()));
			etiquetaDuracion.setText(String.format(Locale.ROOT, "%.0f min", c.duracionMinutos()));
			etiquetaCosto.setText(String.format(Locale.ROOT, "$ %.2f", c.costo()));
			botonReservar.setDisable(false);

		} catch (IllegalArgumentException ex) {

			invalidarCotizacion();
			Alertas.error("No se pudo cotizar", ex.getMessage());
		}
	}

	@FXML
	private void reservar() {

		try {

			Viaje viaje = modelo.reservarViaje(comboServicio.getValue(), leerOrigen(), leerDestino());

			Alertas.info("Reserva registrada", "Tu viaje quedo en estado " + viaje.estadoActual() + ".\nCodigo: "
					+ codigoCorto(viaje));

			invalidarCotizacion();
			pestanias.getSelectionModel().select(1);
			tablaReservas.getSelectionModel().select(viaje);

		} catch (IllegalArgumentException | IllegalStateException ex) {

			Alertas.error("No se pudo reservar", ex.getMessage());
		}
	}

	@FXML
	private void cancelarReserva() {

		Viaje viaje = tablaReservas.getSelectionModel().getSelectedItem();

		try {

			modelo.cancelarReserva(viaje);
			actualizarBotonCancelar();

		} catch (IllegalStateException ex) {

			Alertas.error("No se pudo cancelar", ex.getMessage());
		}
	}

	@FXML
	private void cerrarSesion() {

		modelo.cerrarSesion();
		navegador.mostrarLogin();
	}

	// ------------------------------------------------------------------ formulario

	private Ubicacion leerOrigen() {

		return leerUbicacion(txtLatOrigen, txtLonOrigen, "origen");
	}

	private Ubicacion leerDestino() {

		return leerUbicacion(txtLatDestino, txtLonDestino, "destino");
	}

	private Ubicacion leerUbicacion(TextField campoLat, TextField campoLon, String nombre) {

		try {

			return new Ubicacion(numero(campoLat), numero(campoLon));

		} catch (NumberFormatException ex) {

			throw new IllegalArgumentException("Latitud y longitud del " + nombre + " deben ser numeros (ej: -42.77).");
		}
	}

	private double numero(TextField campo) {

		return Double.parseDouble(campo.getText().trim().replace(',', '.'));
	}

	private void completar(Ubicacion ubicacion, TextField campoLat, TextField campoLon) {

		campoLat.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLatitud()));
		campoLon.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLongitud()));
	}

	private void invalidarCotizacion() {

		botonReservar.setDisable(true);
		etiquetaDistancia.setText("-");
		etiquetaDuracion.setText("-");
		etiquetaCosto.setText("-");
	}

	// ------------------------------------------------------------------ reservas

	private void actualizarBotonCancelar() {

		Viaje elegido = tablaReservas.getSelectionModel().getSelectedItem();
		botonCancelar.setDisable(elegido == null || elegido.estadoActual() != EstadoViaje.SOLICITADO);
	}

	private void configurarTablaReservas() {

		colResId.setCellValueFactory(c -> new SimpleStringProperty(codigoCorto(c.getValue())));
		colResFecha.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getRegistroViaje().get(0).getFechaHora().format(FORMATO_FECHA)));
		colResServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getServicio().getNombre()));
		colResOrigen.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getOrigen())));
		colResDestino.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDestino())));
		colResDistancia.setCellValueFactory(c -> new SimpleStringProperty(String.format(Locale.ROOT, "%.2f km",
				c.getValue().getOrigen().calcularDistancia(c.getValue().getDestino()))));
		colResCosto.setCellValueFactory(c -> new SimpleStringProperty(costo(c.getValue())));
		colResEstado.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().estadoActual())));

		tablaReservas.setItems(modelo.getReservas());
	}

	private String costo(Viaje viaje) {

		try {

			return String.format(Locale.ROOT, "$ %.2f",
					modelo.cotizar(viaje.getServicio(), viaje.getOrigen(), viaje.getDestino()).costo());

		} catch (IllegalArgumentException ex) {

			return "-";
		}
	}

	private String codigoCorto(Viaje viaje) {

		return viaje.getId().toString().substring(0, 8);
	}

	private String texto(Ubicacion u) {

		return String.format(Locale.ROOT, "%.5f, %.5f", u.getLatitud(), u.getLongitud());
	}
	public void inicializarMapa() {
	    WebEngine webEngine = webViewMapa.getEngine();
	    
	    webEngine.setJavaScriptEnabled(true);
	    
	    PuenteSeleccionMapa puente = new PuenteSeleccionMapa(this);
	    webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
	        if (newState == Worker.State.SUCCEEDED) {
	            netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
	            window.setMember("javaBridge", puente);
	        }
	    });
	    
	    // Cargar el archivo mapa.html local
	    webEngine.load(getClass().getResource("/html/mapa.html").toExternalForm());
	}
}
