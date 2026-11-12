/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelCliente.fxml. El usuario elige origen y destino (generados al azar o con clics en el
 * mapa), cotiza y solicita el viaje. Coordina la logica (ValidacionUbicaciones, Movilidad,
 * PlanificacionViajeService), el mapa (VisualizadorMapa) y los conductores simulados que corren en hilos
 * (ContextoAplicacion / GestorViajes). No implementa algoritmos viales ni conoce Leaflet.
 */
package ui.impl.javafx.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import aplicacion.ContextoAplicacion;
import datos.CargarParametros;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import logica.Asignacion;
import logica.Movilidad;
import logica.PlanificacionViajeService;
import logica.ValidacionUbicaciones;
import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import rutas.api.RutasViaje;
import servicio.Servicio;
import ubicacion.Ubicacion;
import ui.api.VisualizadorMapa;
import ui.impl.javafx.VistasFX;
import ui.impl.javafx.map.VisualizadorRutasLeaflet;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.RegistroViaje;
import viajes.Viaje;

public class PanelClienteController {

	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final ContextoAplicacion contexto;

	@FXML private Label lblUsuario;
	@FXML private Button btnCerrarSesion;
	@FXML private Node panelFormulario;

	@FXML private ComboBox<Servicio> cmbServicios;
	@FXML private ToggleButton swModoMapa;
	@FXML private Label lblPaso;
	@FXML private TextField txtLatOrigen;
	@FXML private TextField txtLonOrigen;
	@FXML private TextField txtLatDestino;
	@FXML private TextField txtLonDestino;
	@FXML private Button btnGenerarOrigen;
	@FXML private Button btnGenerarDestino;
	@FXML private Button btnCotizar;
	@FXML private Button btnSolicitar;
	@FXML private Label lblDistancia;
	@FXML private Label lblTiempo;
	@FXML private Label lblCosto;
	@FXML private Label lblRecogida;
	@FXML private Label lblTraslado;
	@FXML private TextArea txtAreaEstado;
	@FXML private WebView mapaWeb;

	private VisualizadorMapa visualizador;

	// estado del formulario
	private boolean origenConfirmadoEnMapa;
	private boolean destinoConfirmadoEnMapa;
	private boolean actualizandoCampos;
	private boolean cotizacionVigente;
	private Ubicacion origenValido;
	private Ubicacion destinoValido;
	private Servicio servicioCotizado;

	// viaje en curso (lo avanzan los hilos de los conductores simulados)
	private Viaje viajeActual;
	private Vehiculo vehiculoPlanificado;

	public PanelClienteController(ContextoAplicacion contexto) {

		this.contexto = contexto;
	}

	@FXML
	private void initialize() {

		Usuario usuario = contexto.getUsuarioLogueado();
		lblUsuario.setText("Usuario: " + usuario.getNombre() + " (" + usuario.getEmail() + ")");

		cmbServicios.setItems(javafx.collections.FXCollections.observableArrayList(contexto.getServicios()));
		cmbServicios.setConverter(new StringConverter<>() {

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
		cmbServicios.valueProperty().addListener((obs, a, b) -> invalidarCotizacion());

		for (TextField campo : new TextField[] { txtLatOrigen, txtLonOrigen, txtLatDestino, txtLonDestino }) {

			campo.textProperty().addListener((obs, a, b) -> {

				if (!actualizandoCampos) {

					invalidarCotizacion();
				}
			});
		}

		visualizador = new VisualizadorRutasLeaflet(mapaWeb);
		visualizador.configurarSeleccion(this::recibirPuntoMapa);

		// Los hilos de los conductores avisan aca cuando cambia un viaje: se pasa al hilo de JavaFX.
		contexto.setObservadorViajes(viaje -> Platform.runLater(() -> alCambiarViaje(viaje)));
		contexto.iniciarSimulacion();

		invalidarCotizacion();
	}

	// ------------------------------------------------------------------ ubicaciones

	@FXML
	private void handleCambiarModoUbicaciones() {

		invalidarCotizacion();

		if (swModoMapa.isSelected()) {

			iniciarSeleccionMapa();

		} else {

			visualizador.cancelarSeleccion();
			visualizador.limpiarSeleccion();
			origenConfirmadoEnMapa = false;
			destinoConfirmadoEnMapa = false;
			modoCampos(false);
			lblPaso.setText("");
		}

		actualizarBotones();
	}

	/** Primera ida: vacia el formulario y le pide al mapa que espere el clic del origen. */
	private void iniciarSeleccionMapa() {

		limpiarCampos();
		origenConfirmadoEnMapa = false;
		destinoConfirmadoEnMapa = false;
		modoCampos(true);
		visualizador.limpiarSeleccion();
		visualizador.limpiarRutas();

		lblPaso.setText("Paso 1 de 2: hace clic en el mapa para elegir el ORIGEN.");
		visualizador.activarSeleccion("origen");
	}

	/**
	 * Vuelta del mapa: llega el clic (ya en el hilo de JavaFX). Se valida y ajusta a calle; si es valido se
	 * completa el formulario, se dibuja el marcador y se pasa al paso siguiente; si no, se avisa y se sigue
	 * esperando el mismo paso.
	 */
	void recibirPuntoMapa(String tipo, Ubicacion clic) {

		if (!swModoMapa.isSelected()) {

			return;
		}

		try {

			Ubicacion sobreCalle = ValidacionUbicaciones.validarYAjustar(clic, contexto.getMotorVial());

			if ("origen".equals(tipo)) {

				completarCoordenadas(sobreCalle, txtLatOrigen, txtLonOrigen);
				visualizador.mostrarMarcadorSeleccion("origen", sobreCalle);
				origenConfirmadoEnMapa = true;
				lblPaso.setText("Paso 2 de 2: hace clic en el mapa para elegir el DESTINO.");
				visualizador.activarSeleccion("destino");

			} else {

				completarCoordenadas(sobreCalle, txtLatDestino, txtLonDestino);
				visualizador.mostrarMarcadorSeleccion("destino", sobreCalle);
				destinoConfirmadoEnMapa = true;
				visualizador.cancelarSeleccion();
				lblPaso.setText("Origen y destino confirmados. Ya podes cotizar.");
			}

			invalidarCotizacion();

		} catch (IllegalArgumentException ex) {

			Alertas.advertencia("Punto no valido", ex.getMessage());
			visualizador.activarSeleccion(tipo); // se mantiene el mismo paso
		}
	}

	@FXML
	private void handleGenerarOrigenAleatorio() {

		generarAleatorio("origen", txtLatOrigen, txtLonOrigen);
	}

	@FXML
	private void handleGenerarDestinoAleatorio() {

		generarAleatorio("destino", txtLatDestino, txtLonDestino);
	}

	private void generarAleatorio(String tipo, TextField campoLat, TextField campoLon) {

		try {

			Ubicacion ubicacion = contexto.getGeneradorUbicaciones().generar();

			completarCoordenadas(ubicacion, campoLat, campoLon);
			visualizador.mostrarMarcadorSeleccion(tipo, ubicacion);
			invalidarCotizacion();

		} catch (IllegalStateException ex) {

			Alertas.error("No se pudo generar la ubicacion", ex.getMessage());
		}
	}

	// ------------------------------------------------------------------ cotizar y solicitar

	@FXML
	private void handleCotizar() {

		try {

			Servicio servicio = cmbServicios.getValue();

			if (servicio == null) {

				throw new IllegalArgumentException("Elegi un servicio.");
			}

			Ubicacion origen = ValidacionUbicaciones.validarYAjustar(
					ValidacionUbicaciones.validarCoordenadas(txtLatOrigen.getText(), txtLonOrigen.getText(), "Origen"),
					contexto.getMotorVial());
			Ubicacion destino = ValidacionUbicaciones.validarYAjustar(ValidacionUbicaciones
					.validarCoordenadas(txtLatDestino.getText(), txtLonDestino.getText(), "Destino"),
					contexto.getMotorVial());

			double km = origen.calcularDistancia(destino);

			if (km <= 0) {

				throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo punto.");
			}

			double minutos = km / CargarParametros.getVelocidadMediaKmh() * 60.0;
			double costo = servicio.calcularCosto(km, minutos);

			// se muestran las posiciones ya ajustadas a calle: son las que despues usa el motor de rutas
			completarCoordenadas(origen, txtLatOrigen, txtLonOrigen);
			completarCoordenadas(destino, txtLatDestino, txtLonDestino);
			visualizador.mostrarMarcadorSeleccion("origen", origen);
			visualizador.mostrarMarcadorSeleccion("destino", destino);

			lblDistancia.setText(String.format(Locale.ROOT, "%.2f km (en linea recta)", km));
			lblTiempo.setText(String.format(Locale.ROOT, "%.0f min", minutos));
			lblCosto.setText(String.format(Locale.ROOT, "$ %.2f", costo));

			origenValido = origen;
			destinoValido = destino;
			servicioCotizado = servicio;
			cotizacionVigente = true;
			actualizarBotones();

		} catch (IllegalArgumentException ex) {

			invalidarCotizacion();
			Alertas.advertencia("No se pudo cotizar", ex.getMessage());
		}
	}

	@FXML
	private void handleSolicitarViaje() {

		if (!cotizacionVigente || viajeActual != null) {

			return;
		}

		Usuario cliente = contexto.getUsuarioLogueado();

		// 1) Movilidad elige el conductor compatible mas cercano entre los que atienden la cola
		Optional<Asignacion> asignacion = Movilidad.seleccionarConductor(contexto.getConductoresSimulados(),
				servicioCotizado, origenValido);

		if (asignacion.isEmpty()) {

			Alertas.error("Sin conductores", "No hay conductores disponibles para ese servicio en este momento.");
			return;
		}

		// 2) se planifican los dos tramos viales (antes de pedir el viaje, asi un error no deja nada a medias)
		RutasViaje rutas;

		try {

			rutas = new PlanificacionViajeService(contexto.getMotorVial()).planificar(asignacion.get().vehiculo(),
					origenValido, destinoValido);

		} catch (RutaNoDisponibleException | IllegalStateException ex) {

			Alertas.error("No se pudo planificar la ruta", ex.getMessage());
			return;
		}

		// 3) se crea y solicita el viaje y se deja en la cola: lo toma el hilo de un conductor simulado
		Viaje viaje = new Viaje(cliente, origenValido, destinoValido, servicioCotizado);

		try {

			viaje.solicitar(LocalDateTime.now());

		} catch (IllegalStateException ex) {

			Alertas.error("No se pudo solicitar el viaje", ex.getMessage());
			return;
		}

		viajeActual = viaje;
		vehiculoPlanificado = asignacion.get().vehiculo();

		if (!contexto.solicitarViaje(viaje)) {

			viajeActual = null;
			viaje.cancelar(LocalDateTime.now(), cliente, "Sin capacidad");
			Alertas.error("No se pudo solicitar el viaje",
					"El sistema no puede tomar la solicitud en este momento. Intenta de nuevo en unos segundos.");
			return;
		}

		// 4) el mapa dibuja los dos tramos y se informan las metricas viales de cada uno
		dibujar(rutas, vehiculoPlanificado);
		cotizacionVigente = false;
		bloquearFormulario(true);
		mostrarEstado(viaje);
	}

	/**
	 * Aviso de un hilo de conductor (ya en el hilo de JavaFX): cambio el estado del viaje. Si lo tomo un
	 * conductor distinto del elegido por cercania, se recalcula y redibuja la ruta para el vehiculo real.
	 */
	private void alCambiarViaje(Viaje viaje) {

		if (viaje != viajeActual) {

			return;
		}

		EstadoViaje estado = viaje.estadoActual();
		mostrarEstado(viaje);

		if (estado == EstadoViaje.ACEPTADO && viaje.getVehiculo() != null
				&& viaje.getVehiculo() != vehiculoPlanificado) {

			replanificar(viaje);
		}

		if (estado == EstadoViaje.FINALIZADO || estado == EstadoViaje.CANCELADO || estado == EstadoViaje.RECHAZADO) {

			viajeActual = null;
			vehiculoPlanificado = null;
			bloquearFormulario(false);

			if (estado != EstadoViaje.FINALIZADO) {

				visualizador.limpiarRutas();
				lblRecogida.setText("-");
				lblTraslado.setText("-");
			}
		}
	}

	private void replanificar(Viaje viaje) {

		try {

			RutasViaje rutas = new PlanificacionViajeService(contexto.getMotorVial()).planificar(viaje.getVehiculo(),
					viaje.getOrigen(), viaje.getDestino());

			vehiculoPlanificado = viaje.getVehiculo();
			dibujar(rutas, vehiculoPlanificado);

		} catch (RutaNoDisponibleException | IllegalStateException ex) {

			txtAreaEstado.appendText("\nNo se pudo recalcular la ruta del conductor asignado: " + ex.getMessage());
		}
	}

	private void dibujar(RutasViaje rutas, Vehiculo vehiculo) {

		visualizador.mostrar(rutas, vehiculo.getUbicacion(), origenValido, destinoValido);
		lblRecogida.setText(descripcion(rutas.haciaOrigen()));
		lblTraslado.setText(descripcion(rutas.haciaDestino()));
	}

	private String descripcion(RutaCalculada ruta) {

		return String.format(Locale.ROOT, "%.2f km, %.0f min", ruta.distanciaKm(), ruta.duracionMinutos());
	}

	private void mostrarEstado(Viaje viaje) {

		List<RegistroViaje> registros;

		synchronized (viaje) {

			registros = new ArrayList<>(viaje.getRegistroViaje());
		}

		StringBuilder sb = new StringBuilder();

		for (RegistroViaje registro : registros) {

			sb.append(registro.getFechaHora().format(HORA)).append("  ").append(registro.getEstadoViaje()).append('\n');
		}

		Usuario conductor = viaje.getConductor();
		Vehiculo vehiculo = viaje.getVehiculo();

		if (conductor != null && vehiculo != null) {

			sb.append("Conductor: ").append(conductor.getNombre()).append(" - ").append(vehiculo.getModelo())
					.append(" (").append(vehiculo.getPatente()).append(")\n");
		}

		sb.append("Estado actual: ").append(viaje.estadoActual());

		txtAreaEstado.setText(sb.toString());
	}

	// ------------------------------------------------------------------ sesion

	@FXML
	private void handleCerrarSesion() {

		contexto.setObservadorViajes(null);
		contexto.cerrarSesion(); // detiene los hilos y cancela el viaje en curso

		VistasFX.mostrar((Stage) btnCerrarSesion.getScene().getWindow(), "Login.fxml", "Movilidad Digital - Ingreso",
				contexto);
	}

	// ------------------------------------------------------------------ estado del formulario

	/** Cualquier cambio (ubicacion, servicio, modo) descarta la cotizacion y los recorridos anteriores. */
	private void invalidarCotizacion() {

		if (viajeActual != null) {

			return; // con un viaje en curso el formulario esta bloqueado
		}

		cotizacionVigente = false;
		lblDistancia.setText("-");
		lblTiempo.setText("-");
		lblCosto.setText("-");
		lblRecogida.setText("-");
		lblTraslado.setText("-");

		if (visualizador != null) {

			visualizador.limpiarRutas();
		}

		actualizarBotones();
	}

	private void actualizarBotones() {

		boolean camposCompletos = !txtLatOrigen.getText().isBlank() && !txtLonOrigen.getText().isBlank()
				&& !txtLatDestino.getText().isBlank() && !txtLonDestino.getText().isBlank();
		boolean listoParaCotizar = camposCompletos
				&& (!swModoMapa.isSelected() || (origenConfirmadoEnMapa && destinoConfirmadoEnMapa));

		btnCotizar.setDisable(!listoParaCotizar);
		btnSolicitar.setDisable(!cotizacionVigente || viajeActual != null);
	}

	private void bloquearFormulario(boolean bloquear) {

		panelFormulario.setDisable(bloquear);

		if (!bloquear) {

			actualizarBotones();
		}
	}

	/** En modo mapa los campos son de solo lectura y no se generan puntos al azar. */
	private void modoCampos(boolean modoMapa) {

		for (TextField campo : new TextField[] { txtLatOrigen, txtLonOrigen, txtLatDestino, txtLonDestino }) {

			campo.setEditable(!modoMapa);
		}

		btnGenerarOrigen.setDisable(modoMapa);
		btnGenerarDestino.setDisable(modoMapa);
	}

	private void limpiarCampos() {

		actualizandoCampos = true;
		txtLatOrigen.clear();
		txtLonOrigen.clear();
		txtLatDestino.clear();
		txtLonDestino.clear();
		actualizandoCampos = false;
	}

	private void completarCoordenadas(Ubicacion ubicacion, TextField campoLat, TextField campoLon) {

		actualizandoCampos = true;
		campoLat.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLatitud()));
		campoLon.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLongitud()));
		actualizandoCampos = false;
	}
}
