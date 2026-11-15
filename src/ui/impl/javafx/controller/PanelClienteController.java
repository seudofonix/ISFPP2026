/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelCliente.fxml. El usuario elige origen y destino con clics en el mapa (se le muestran la
 * calle y la altura, no las coordenadas), cotiza, solicita el viaje y puede cancelarlo. Coordina la logica
 * (ValidacionUbicaciones, Movilidad, PlanificacionViajeService), el mapa (VisualizadorMapa) y los conductores
 * simulados que corren en hilos (ContextoAplicacion / Simulacion). No implementa algoritmos viales ni conoce
 * Leaflet.
 */
package ui.impl.javafx.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import aplicacion.ContextoAplicacion;
import aplicacion.ContextoAplicacion.Resultado;
import aplicacion.ContextoAplicacion.SolicitudViaje;
import datos.CargarParametros;
import idioma.Mensajes;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import logica.Cotizador;
import modelo.Cotizacion;
import logica.PlanificacionViajeService;
import logica.ValidacionUbicaciones;
import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import rutas.api.RutasViaje;
import servicio.Servicio;
import ubicacion.Ubicacion;
import ui.Formato;
import ui.api.VisualizadorMapa;
import ui.impl.javafx.VistasFX;
import ui.impl.javafx.map.VisualizadorRutasLeaflet;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
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
	@FXML private Button btnElegirPuntos;
	@FXML private Label lblPaso;
	@FXML private TextField txtOrigen;
	@FXML private TextField txtDestino;
	@FXML private Button btnCotizar;
	@FXML private Button btnSolicitar;
	@FXML private Button btnCancelarViaje;
	@FXML private Label lblDistancia;
	@FXML private Label lblTiempo;
	@FXML private Label lblCosto;
	@FXML private Label lblRecogida;
	@FXML private Label lblTraslado;
	@FXML private TextArea txtAreaEstado;
	@FXML private WebView mapaWeb;

	private VisualizadorMapa visualizador;

	// estado del formulario (origen y destino ya ajustados a calle; null hasta que se eligen en el mapa)
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
		lblUsuario.setText(Mensajes.get("cliente.usuario", usuario.getNombre(), usuario.getEmail()));

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

		visualizador = new VisualizadorRutasLeaflet(mapaWeb);
		visualizador.configurarSeleccion(this::recibirPuntoMapa);

		// Los hilos de los conductores avisan aca cuando cambia un viaje: se pasa al hilo de JavaFX.
		contexto.setObservadorViajes(viaje -> Platform.runLater(() -> alCambiarViaje(viaje)));
		contexto.iniciarSimulacion();

		iniciarSeleccionMapa();
	}

	// ------------------------------------------------------------------ ubicaciones

	@FXML
	private void handleElegirPuntos() {

		iniciarSeleccionMapa();
	}

	/** Vacia origen y destino y le pide al mapa que espere el clic del origen. */
	private void iniciarSeleccionMapa() {

		if (viajeActual != null) {

			return;
		}

		origenValido = null;
		destinoValido = null;
		txtOrigen.clear();
		txtDestino.clear();
		visualizador.limpiarSeleccion();
		invalidarCotizacion();
		lblPaso.setText(Mensajes.get("cliente.paso1"));
		visualizador.activarSeleccion("origen");
	}

	/**
	 * Vuelta del mapa: llega el clic (ya en el hilo de JavaFX). Se valida y ajusta a calle; si es valido se
	 * muestra la direccion (calle y altura), se dibuja el marcador y se pasa al paso siguiente; si no, se avisa y
	 * se sigue esperando el mismo paso.
	 */
	void recibirPuntoMapa(String tipo, Ubicacion clic) {

		if (viajeActual != null) {

			return;
		}

		try {

			Ubicacion sobreCalle = ValidacionUbicaciones.validarYAjustar(clic, contexto.getMotorVial());
			String direccion = contexto.getGeocodificador().direccion(sobreCalle);

			if ("origen".equals(tipo)) {

				origenValido = sobreCalle;
				txtOrigen.setText(direccion);
				visualizador.mostrarMarcadorSeleccion("origen", sobreCalle);
				lblPaso.setText(Mensajes.get("cliente.paso2"));
				visualizador.activarSeleccion("destino");

			} else {

				destinoValido = sobreCalle;
				txtDestino.setText(direccion);
				visualizador.mostrarMarcadorSeleccion("destino", sobreCalle);
				visualizador.cancelarSeleccion();
				lblPaso.setText(Mensajes.get("cliente.pasoListo"));
			}

			invalidarCotizacion();

		} catch (IllegalArgumentException ex) {

			Alertas.advertencia(Mensajes.get("cliente.puntoInvalido"), ex.getMessage());
			visualizador.activarSeleccion(tipo); // se mantiene el mismo paso
		}
	}

	// ------------------------------------------------------------------ cotizar y solicitar

	@FXML
	private void handleCotizar() {

		try {

			Servicio servicio = cmbServicios.getValue();
			Cotizacion cotizacion = Cotizador.cotizar(servicio, origenValido, destinoValido);

			lblDistancia.setText(Mensajes.get("cliente.distanciaRecta", cotizacion.distanciaKm()));
			lblTiempo.setText(Mensajes.get("cliente.minutos", cotizacion.duracionMinutos()));
			lblCosto.setText(Mensajes.get("cliente.precio", cotizacion.costo()));

			servicioCotizado = servicio;
			cotizacionVigente = true;
			actualizarBotones();

		} catch (IllegalArgumentException ex) {

			invalidarCotizacion();
			Alertas.advertencia(Mensajes.get("cliente.noSePudoCotizar"), ex.getMessage());
		}
	}

	@FXML
	private void handleSolicitarViaje() {

		if (!cotizacionVigente || viajeActual != null) {

			return;
		}

		Usuario cliente = contexto.getUsuarioLogueado();

		// Movilidad elige el conductor, se planifican los tramos y se deja el viaje en la cola (o se rechaza)
		SolicitudViaje solicitud;

		try {

			solicitud = contexto.pedirViaje(cliente, servicioCotizado, origenValido, destinoValido);

		} catch (RutaNoDisponibleException | IllegalStateException ex) {

			Alertas.error(Mensajes.get("cliente.noSePudoSolicitar.titulo"), ex.getMessage());
			return;
		}

		if (solicitud.resultado() == Resultado.SIN_CONDUCTORES) {

			cotizacionVigente = false;
			actualizarBotones();
			mostrarEstado(solicitud.viaje());
			Alertas.informacion(Mensajes.get("cliente.reservaRechazada.titulo"), Mensajes.get("cliente.reservaRechazada"));
			return;
		}

		if (solicitud.resultado() == Resultado.COLA_LLENA) {

			Alertas.error(Mensajes.get("cliente.noSePudoSolicitar.titulo"), Mensajes.get("cliente.colaLlena"));
			return;
		}

		Viaje viaje = solicitud.viaje();
		viajeActual = viaje;
		vehiculoPlanificado = solicitud.vehiculo();
		RutasViaje rutas = solicitud.rutas();

		// 4) el mapa dibuja los dos tramos y se informan las metricas viales de cada uno
		dibujar(rutas, vehiculoPlanificado);
		cotizacionVigente = false;
		bloquearFormulario(true);
		mostrarEstado(viaje);
	}

	// ------------------------------------------------------------------ cancelacion

	@FXML
	private void handleCancelarViaje() {

		Viaje viaje = viajeActual;

		if (viaje == null) {

			return;
		}

		TextInputDialog dialogo = new TextInputDialog();
		dialogo.setTitle(Mensajes.get("cliente.cancelar.titulo"));
		dialogo.setHeaderText(Mensajes.get("cliente.cancelar.pregunta"));
		dialogo.setContentText(Mensajes.get("cliente.cancelar.motivo"));
		Optional<String> respuesta = dialogo.showAndWait();

		if (respuesta.isEmpty()) {

			return; // cerro el dialogo: el viaje sigue
		}

		String motivo = respuesta.get().isBlank() ? Mensajes.get("cliente.motivoPorDefecto") : respuesta.get().trim();

		try {

			contexto.cancelarViaje(viaje, contexto.getUsuarioLogueado(), motivo);

		} catch (IllegalStateException ex) {

			Alertas.advertencia(Mensajes.get("cliente.noSePudoCancelar.titulo"), ex.getMessage());
		}
	}

	// ------------------------------------------------------------------ avisos de los conductores

	/**
	 * Aviso de un hilo de conductor o de una accion del usuario (ya en el hilo de JavaFX): cambio el estado del
	 * viaje. Si lo tomo un conductor distinto del elegido por cercania, se recalcula y redibuja la ruta para el
	 * vehiculo real. El marcador del conductor se mueve por cada tramo durante el tiempo que dura en la
	 * simulacion.
	 */
	private void alCambiarViaje(Viaje viaje) {

		if (viaje != viajeActual) {

			return;
		}

		EstadoViaje estado = viaje.estadoActual();
		mostrarEstado(viaje);

		if (estado == EstadoViaje.ACEPTADO) {

			if (viaje.getVehiculo() != null && viaje.getVehiculo() != vehiculoPlanificado) {

				replanificar(viaje);
			}

			visualizador.animarConductor("acercamiento", contexto.segundosDelTramo(viaje, EstadoViaje.ACEPTADO));

		} else if (estado == EstadoViaje.INICIADO) {

			visualizador.animarConductor("traslado", contexto.segundosDelTramo(viaje, EstadoViaje.INICIADO));
		}

		if (estado == EstadoViaje.FINALIZADO || estado == EstadoViaje.CANCELADO || estado == EstadoViaje.RECHAZADO) {

			viajeActual = null;
			vehiculoPlanificado = null;
			bloquearFormulario(false);

			if (estado != EstadoViaje.FINALIZADO) {

				visualizador.detenerAnimacion();
				visualizador.limpiarRutas();
				lblRecogida.setText("-");
				lblTraslado.setText("-");
			}

			if (estado == EstadoViaje.RECHAZADO) {

				Alertas.informacion(Mensajes.get("cliente.reservaRechazada.titulo"),
						Mensajes.get("cliente.sinRespuesta"));
			}
		}

		actualizarCancelar();

		if (estado == EstadoViaje.FINALIZADO) {

			// el viaje termino bien: el cliente tiene que calificar al conductor antes de seguir
			Platform.runLater(() -> exigirCalificacion(viaje));
		}
	}

	/**
	 * Dialogo de calificacion obligatorio: el boton Enviar se habilita al elegir una opcion y, si el usuario
	 * cierra la ventana sin calificar, se vuelve a mostrar.
	 */
	private void exigirCalificacion(Viaje viaje) {

		if (!viaje.requiereCalificacion()) {

			return;
		}

		Usuario conductor = viaje.getConductor();
		String nombre = conductor == null ? "" : conductor.getNombre();
		CalificacionViaje elegida = null;
		String comentario = "";

		while (elegida == null) {

			Dialog<CalificacionViaje> dialogo = new Dialog<>();
			dialogo.setTitle(Mensajes.get("cliente.calificar.titulo"));
			dialogo.setHeaderText(Mensajes.get("cliente.calificar.pregunta", nombre));

			ButtonType enviar = new ButtonType(Mensajes.get("cliente.calificar.enviar"), ButtonBar.ButtonData.OK_DONE);
			dialogo.getDialogPane().getButtonTypes().add(enviar);
			Node botonEnviar = dialogo.getDialogPane().lookupButton(enviar);
			botonEnviar.setDisable(true);

			ToggleGroup grupo = new ToggleGroup();
			VBox opciones = new VBox(6);

			for (int valor = 5; valor >= 1; valor--) {

				CalificacionViaje c = CalificacionViaje.values()[valor];
				RadioButton opcion = new RadioButton(Formato.calificacionConNombre(c));
				opcion.setUserData(c);
				opcion.setToggleGroup(grupo);
				opciones.getChildren().add(opcion);
			}

			// casilla de comentario opcional, limitada al largo que acepta el modelo
			TextArea casillaComentario = new TextArea();
			casillaComentario.setPromptText(Mensajes.get("cliente.calificar.comentario"));
			casillaComentario.setPrefRowCount(3);
			casillaComentario.setWrapText(true);
			casillaComentario.setTextFormatter(new TextFormatter<String>(
					cambio -> cambio.getControlNewText().length() <= Viaje.MAX_COMENTARIO ? cambio : null));
			opciones.getChildren().add(casillaComentario);

			grupo.selectedToggleProperty().addListener((obs, a, b) -> botonEnviar.setDisable(b == null));
			dialogo.getDialogPane().setContent(opciones);
			dialogo.setResultConverter(boton -> grupo.getSelectedToggle() == null ? null
					: (CalificacionViaje) grupo.getSelectedToggle().getUserData());

			elegida = dialogo.showAndWait().orElse(null);
			comentario = casillaComentario.getText();
		}

		try {

			contexto.calificarViaje(viaje, elegida, comentario);
			mostrarEstado(viaje);

		} catch (IllegalStateException | IllegalArgumentException ex) {

			Alertas.error(Mensajes.get("cliente.calificar.titulo"), ex.getMessage());
		}
	}

	private void replanificar(Viaje viaje) {

		try {

			RutasViaje rutas = new PlanificacionViajeService(contexto.getMotorVial()).planificar(viaje.getVehiculo(),
					viaje.getOrigen(), viaje.getDestino());
			vehiculoPlanificado = viaje.getVehiculo();
			dibujar(rutas, vehiculoPlanificado);

		} catch (RutaNoDisponibleException | IllegalStateException ex) {

			txtAreaEstado.appendText("\n" + Mensajes.get("cliente.errorReplanificar", ex.getMessage()));
		}
	}

	private void dibujar(RutasViaje rutas, Vehiculo vehiculo) {

		visualizador.mostrar(rutas, vehiculo.getUbicacion(), origenValido, destinoValido);
		lblRecogida.setText(descripcion(rutas.haciaOrigen()));
		lblTraslado.setText(descripcion(rutas.haciaDestino()));
	}

	private String descripcion(RutaCalculada ruta) {

		return Mensajes.get("cliente.kmMin", ruta.distanciaKm(), ruta.duracionMinutos());
	}

	private void mostrarEstado(Viaje viaje) {

		List<RegistroViaje> registros;
		String motivo;

		synchronized (viaje) {

			registros = new ArrayList<>(viaje.getRegistroViaje());
			motivo = viaje.getMotivoCancelacion();
		}

		StringBuilder sb = new StringBuilder();

		for (RegistroViaje registro : registros) {

			sb.append(registro.getFechaHora().format(HORA)).append("  ").append(Mensajes.get("estado." + registro.getEstadoViaje().name())).append('\n');
		}

		Usuario conductor = viaje.getConductor();
		Vehiculo vehiculo = viaje.getVehiculo();

		if (conductor != null && vehiculo != null) {

			sb.append(Mensajes.get("cliente.conductorAsignado", conductor.getNombre(), vehiculo.getModelo(),
					vehiculo.getPatente())).append('\n');
		}

		EstadoViaje estado = viaje.estadoActual();
		sb.append(Mensajes.get("cliente.estadoActual", Mensajes.get("estado." + estado.name()), descripcion(estado)));

		if (estado == EstadoViaje.FINALIZADO && !viaje.requiereCalificacion()) {

			sb.append("\n").append(Mensajes.get("cliente.calificacion",
					Formato.calificacionConNombre(viaje.getCalificacionConductor())));

			if (!viaje.getComentarioCalificacion().isEmpty()) {

				sb.append("\n").append(Mensajes.get("cliente.comentario", viaje.getComentarioCalificacion()));
			}
		}

		if (estado == EstadoViaje.CANCELADO && motivo != null) {

			sb.append("\n").append(Mensajes.get("cliente.motivo", motivo));
		}

		txtAreaEstado.setText(sb.toString());
	}

	private String descripcion(EstadoViaje estado) {

		return Mensajes.get("estado.desc." + estado.name());
	}

	// ------------------------------------------------------------------ sesion

	@FXML
	private void handleCerrarSesion() {

		visualizador.detenerAnimacion();
		contexto.setObservadorViajes(null);
		contexto.cerrarSesion(); // detiene los hilos y cancela el viaje en curso
		VistasFX.mostrar((Stage) btnCerrarSesion.getScene().getWindow(), "Login.fxml", Mensajes.get("ventana.login"),
				contexto);
	}

	// ------------------------------------------------------------------ estado del formulario

	/** Cualquier cambio (ubicacion, servicio) descarta la cotizacion y los recorridos anteriores. */
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

		btnCotizar.setDisable(origenValido == null || destinoValido == null);
		btnSolicitar.setDisable(!cotizacionVigente || viajeActual != null);
		actualizarCancelar();
	}

	/** Se puede cancelar mientras el viaje esta buscando conductor, yendo al origen o en curso. */
	private void actualizarCancelar() {

		btnCancelarViaje.setDisable(viajeActual == null);
	}

	private void bloquearFormulario(boolean bloquear) {

		panelFormulario.setDisable(bloquear);

		if (!bloquear) {

			actualizarBotones();
		}

		actualizarCancelar();
	}
}
