/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de PanelCliente.fxml. El usuario elige origen y destino con clics en el mapa (se le muestran la
 * calle y la altura, no las coordenadas), cotiza, solicita el viaje y puede cancelarlo. Coordina la logica
 * (ValidacionUbicaciones, Movilidad, PlanificacionViajeService), el mapa (VisualizadorMapa) y los conductores
 * simulados que corren en hilos (ContextoAplicacion / GestorViajes). No implementa algoritmos viales ni conoce
 * Leaflet.
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
import javafx.scene.control.TextInputDialog;
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
		lblPaso.setText("Paso 1 de 2: hace clic en el mapa para elegir el ORIGEN.");
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
				lblPaso.setText("Paso 2 de 2: hace clic en el mapa para elegir el DESTINO.");
				visualizador.activarSeleccion("destino");

			} else {

				destinoValido = sobreCalle;
				txtDestino.setText(direccion);
				visualizador.mostrarMarcadorSeleccion("destino", sobreCalle);
				visualizador.cancelarSeleccion();
				lblPaso.setText("Origen y destino confirmados. Ya podes cotizar.");
			}

			invalidarCotizacion();

		} catch (IllegalArgumentException ex) {

			Alertas.advertencia("Punto no valido", ex.getMessage());
			visualizador.activarSeleccion(tipo); // se mantiene el mismo paso
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

			if (origenValido == null || destinoValido == null) {

				throw new IllegalArgumentException("Elegi el origen y el destino en el mapa.");
			}

			double km = origenValido.calcularDistancia(destinoValido);

			if (km <= 0) {

				throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo punto.");
			}

			double minutos = km / CargarParametros.getVelocidadMediaKmh() * 60.0;
			double costo = servicio.calcularCosto(km, minutos);

			lblDistancia.setText(String.format(Locale.ROOT, "%.2f km (en linea recta)", km));
			lblTiempo.setText(String.format(Locale.ROOT, "%.0f min", minutos));
			lblCosto.setText(String.format(Locale.ROOT, "$ %.2f", costo));

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

		// 1) Movilidad elige el conductor disponible y compatible mas cercano entre los que atienden la cola
		Optional<Asignacion> asignacion = Movilidad.seleccionarConductor(contexto.getConductoresSimulados(),
				servicioCotizado, origenValido);

		if (asignacion.isEmpty()) {

			rechazarPorFaltaDeConductores(cliente);
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
	 * No hay ningun conductor disponible para el servicio: la reserva queda registrada y se rechaza al instante,
	 * sin esperar el tiempo maximo de espera.
	 */
	private void rechazarPorFaltaDeConductores(Usuario cliente) {

		Viaje viaje = new Viaje(cliente, origenValido, destinoValido, servicioCotizado);

		try {

			viaje.solicitar(LocalDateTime.now());
			contexto.rechazarViaje(viaje);

		} catch (IllegalStateException ex) {

			Alertas.error("No se pudo solicitar el viaje", ex.getMessage());
			return;
		}

		cotizacionVigente = false;
		actualizarBotones();
		mostrarEstado(viaje);
		Alertas.informacion("Reserva rechazada",
				"No hay conductores disponibles para ese servicio en este momento. La reserva fue rechazada.");
	}

	// ------------------------------------------------------------------ cancelacion

	@FXML
	private void handleCancelarViaje() {

		Viaje viaje = viajeActual;

		if (viaje == null) {

			return;
		}

		TextInputDialog dialogo = new TextInputDialog();
		dialogo.setTitle("Cancelar viaje");
		dialogo.setHeaderText("Queres cancelar el viaje?");
		dialogo.setContentText("Motivo (opcional):");
		Optional<String> respuesta = dialogo.showAndWait();

		if (respuesta.isEmpty()) {

			return; // cerro el dialogo: el viaje sigue
		}

		String motivo = respuesta.get().isBlank() ? "Cancelado por el cliente" : respuesta.get().trim();

		try {

			contexto.cancelarViaje(viaje, contexto.getUsuarioLogueado(), motivo);

		} catch (IllegalStateException ex) {

			Alertas.advertencia("No se pudo cancelar", ex.getMessage());
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

			visualizador.animarConductor("acercamiento", CargarParametros.getSegundosTramo());

		} else if (estado == EstadoViaje.INICIADO) {

			visualizador.animarConductor("traslado", CargarParametros.getSegundosTramo());
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

				Alertas.informacion("Reserva rechazada",
						"Ningun conductor tomo el viaje a tiempo. La reserva fue rechazada.");
			}
		}

		actualizarCancelar();
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
		String motivo;

		synchronized (viaje) {

			registros = new ArrayList<>(viaje.getRegistroViaje());
			motivo = viaje.getMotivoCancelacion();
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

		EstadoViaje estado = viaje.estadoActual();
		sb.append("Estado actual: ").append(estado).append(" - ").append(descripcion(estado));

		if (estado == EstadoViaje.CANCELADO && motivo != null) {

			sb.append("\nMotivo: ").append(motivo);
		}

		txtAreaEstado.setText(sb.toString());
	}

	private String descripcion(EstadoViaje estado) {

		return switch (estado) {
			case SOLICITADO -> "buscando conductor";
			case ACEPTADO -> "el conductor va hacia tu origen";
			case INICIADO -> "viaje en curso hacia el destino";
			case FINALIZADO -> "llegaste a destino";
			case CANCELADO -> "viaje cancelado";
			case RECHAZADO -> "no hubo conductor disponible";
		};
	}

	// ------------------------------------------------------------------ sesion

	@FXML
	private void handleCerrarSesion() {

		visualizador.detenerAnimacion();
		contexto.setObservadorViajes(null);
		contexto.cerrarSesion(); // detiene los hilos y cancela el viaje en curso
		VistasFX.mostrar((Stage) btnCerrarSesion.getScene().getWindow(), "Login.fxml", "Movilidad Digital - Ingreso",
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
