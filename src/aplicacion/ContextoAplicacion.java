/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Contexto de la aplicacion. Carga usuarios, vehiculos y servicios (Datos, desde la base o los archivos segun
 * config.properties), mantiene el motor vial, el generador de posiciones y el usuario con la sesion iniciada,
 * y delega en InicializadorEscenario la ubicacion inicial de los conductores. Tambien coordina los hilos de
 * los conductores simulados (GestorViajes) y cierra el grafo al salir. No depende de JavaFX: la interfaz se
 * registra como observador de viajes y refresca la pantalla en su propio hilo.
 */
package aplicacion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import modelo.ConfiguracionSimulacion;
import datos.CargarParametros;
import datos.Datos;
import modelo.GestorViajes;
import idioma.Mensajes;
import logica.Asignacion;
import logica.Movilidad;
import logica.PlanificacionViajeService;
import rutas.api.Geocodificador;
import rutas.api.PlanificadorRutas;
import rutas.api.RutasViaje;
import rutas.impl.GeocodificadorOsm;
import rutas.impl.PlanificadorRutasFactory;
import servicio.Servicio;
import simulacion.GeneradorUbicacionesViales;
import simulacion.InicializadorEscenario;
import ubicacion.Ubicacion;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.CalificacionViaje;
import viajes.Viaje;

public class ContextoAplicacion implements AutoCloseable {

	private final PlanificadorRutas motorVial;
	private final GeneradorUbicacionesViales generadorUbicaciones;
	private final Geocodificador geocodificador;
	private final GestorViajes gestor = new GestorViajes(this::viajeCambio);

	private volatile List<Usuario> usuarios = List.of();
	private volatile List<Vehiculo> vehiculos = List.of();
	private volatile List<Servicio> servicios = List.of();
	private volatile List<Usuario> conductoresSimulados = List.of();

	private volatile Usuario usuarioLogueado;
	private volatile Consumer<Viaje> observadorViajes;

	/** Contexto sin domicilios: las direcciones se informan solo con el nombre de la calle. */
	public ContextoAplicacion(PlanificadorRutas motorVial, GeneradorUbicacionesViales generadorUbicaciones) {

		this(motorVial, generadorUbicaciones, new GeocodificadorOsm(motorVial, null));
	}

	public ContextoAplicacion(PlanificadorRutas motorVial, GeneradorUbicacionesViales generadorUbicaciones,
			Geocodificador geocodificador) {

		this.motorVial = Objects.requireNonNull(motorVial, "motorVial");
		this.generadorUbicaciones = Objects.requireNonNull(generadorUbicaciones, "generadorUbicaciones");
		this.geocodificador = Objects.requireNonNull(geocodificador, "geocodificador");
	}

	/**
	 * Crea el contexto completo: lee config.properties, importa el mapa vial (motor de rutas) y carga los datos.
	 */
	public static ContextoAplicacion crear() throws IOException {

		CargarParametros.parametros();

		PlanificadorRutas motor = PlanificadorRutasFactory.crear();
		ContextoAplicacion contexto = new ContextoAplicacion(motor, new GeneradorUbicacionesViales(motor),
				PlanificadorRutasFactory.crearGeocodificador(motor));

		try {

			contexto.recargarDatos();

		} catch (IOException | RuntimeException e) {

			motor.close();
			throw e;
		}

		return contexto;
	}

	// ------------------------------------------------------------------ datos

	/**
	 * (Re)carga los datos desde el origen configurado. Antes detiene la simulacion y cierra la sesion, porque se
	 * crean objetos nuevos; despues ubica a los conductores sobre calles.
	 */
	public void recargarDatos() throws IOException {

		CargarParametros.parametros();

		HashMap<String, Vehiculo> mapaVehiculos = Datos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
		HashMap<String, Usuario> mapaUsuarios = Datos.cargarUsuarios(CargarParametros.getArchivoUsuarios(),
				mapaVehiculos);
		List<Servicio> listaServicios = Datos.cargarServicios(CargarParametros.getArchivoServicios());

		cargar(mapaUsuarios.values(), mapaVehiculos.values(), listaServicios);
	}

	/** Reemplaza los datos en memoria (ordenados) y deja el escenario inicial listo. */
	public void cargar(Collection<Usuario> nuevosUsuarios, Collection<Vehiculo> nuevosVehiculos,
			Collection<Servicio> nuevosServicios) {

		gestor.detener();
		gestor.limpiar();
		conductoresSimulados = List.of();
		usuarioLogueado = null;

		usuarios = nuevosUsuarios.stream()
				.sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
		vehiculos = nuevosVehiculos.stream().sorted(Comparator.comparing(Vehiculo::getPatente)).toList();
		servicios = new ArrayList<>(nuevosServicios);

		InicializadorEscenario.inicializar(usuarios, generadorUbicaciones);
	}

	/** Origen de los datos segun config.properties: "bd" (base PostgreSQL) o "archivo" (archivos de texto). */
	public String getOrigen() {

		return CargarParametros.getOrigen();
	}

	public List<Usuario> getUsuarios() {

		return usuarios;
	}

	public List<Usuario> getConductoresSimulados() {

		return conductoresSimulados;
	}

	public List<Vehiculo> getVehiculos() {

		return vehiculos;
	}

	public List<Servicio> getServicios() {

		return servicios;
	}

	public PlanificadorRutas getMotorVial() {

		return motorVial;
	}

	/** Convierte un punto del mapa en una direccion legible (calle y altura) para mostrar al usuario. */
	public Geocodificador getGeocodificador() {

		return geocodificador;
	}

	/** El cliente califica al conductor de un viaje finalizado (obligatorio antes de pedir otro viaje). */
	public void calificarViaje(Viaje viaje, CalificacionViaje calificacion) {

		viaje.calificarConductor(calificacion);
	}

	/** Punto al azar sobre una calle del area de simulacion (para interfaces sin mapa donde hacer clic). */
	public Ubicacion generarUbicacionAleatoria() {

		return generadorUbicaciones.generar();
	}

	/** Busca un usuario por su email (sin distinguir mayusculas). */
	public Optional<Usuario> buscarUsuarioPorEmail(String email) {

		if (email == null || email.isBlank()) {

			return Optional.empty();
		}

		String buscado = email.trim();

		return usuarios.stream().filter(u -> u.getEmail().equalsIgnoreCase(buscado)).findFirst();
	}

	// ------------------------------------------------------------------ sesion

	public Usuario getUsuarioLogueado() {

		return usuarioLogueado;
	}

	public void setUsuarioLogueado(Usuario usuario) {

		this.usuarioLogueado = usuario;
	}

	/**
	 * Cierra la sesion: detiene los hilos de simulacion (cancelando los viajes en curso) y, si quien salia era un
	 * conductor que habia pasado a cliente, lo vuelve a poner en servicio.
	 */
	public void cerrarSesion() {

		gestor.detener();
		conductoresSimulados = List.of();

		Usuario saliente = usuarioLogueado;
		usuarioLogueado = null;

		if (saliente != null && saliente.getConductor() != null && saliente.getRolActivo() == RolUsuario.CLIENTE
				&& saliente.getConductor().getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO
				&& !saliente.getCliente().enViaje()) {

			saliente.cambiarRolActivo(RolUsuario.CONDUCTOR);
		}
	}

	// ------------------------------------------------------------------ viajes y simulacion (hilos)

	/**
	 * Arranca un hilo por conductor simulado (hasta simulacion.maximo.conductores, sin contar al usuario de la
	 * sesion). Se eligen para que todos los servicios tengan algun conductor que los pueda tomar. Cada uno espera
	 * viajes en la cola compartida del GestorViajes.
	 */
	public void iniciarSimulacion() {

		conductoresSimulados = Movilidad.elegirConductoresSimulados(usuarios, servicios, usuarioLogueado,
				CargarParametros.getMaximoConductores());

		gestor.iniciar(conductoresSimulados,
				new ConfiguracionSimulacion(CargarParametros.getSegundosTramo(), CargarParametros.getPasosTramo(),
						CargarParametros.getEsperaMaximaSegundos(), CargarParametros.getCapacidadBandeja()));
	}

	/** Que paso con un pedido de viaje. */
	public enum Resultado {
		/** El viaje quedo en la cola y lo toma un conductor simulado. */
		ACEPTADA,
		/** No habia ningun conductor disponible para el servicio: la reserva se rechazo al instante. */
		SIN_CONDUCTORES,
		/** Las bandejas de los conductores estan llenas: el viaje se cancelo y hay que reintentar. */
		COLA_LLENA
	}

	/**
	 * Resultado de pedirViaje. rutas y vehiculo solo estan cuando el pedido fue ACEPTADA (vehiculo es el
	 * conductor elegido por cercania; otro conductor puede terminar tomando el viaje).
	 */
	public record SolicitudViaje(Resultado resultado, Viaje viaje, RutasViaje rutas, Vehiculo vehiculo) {
	}

	/**
	 * Pide un viaje para el cliente: elige el conductor disponible y compatible mas cercano, planifica los dos
	 * tramos viales, crea y solicita el viaje y lo deja en la cola. Si no hay conductor disponible lo rechaza al
	 * instante. Es la misma regla para cualquier interfaz (JavaFX o consola).
	 *
	 * @throws rutas.api.RutaNoDisponibleException si no hay camino entre los puntos (no se crea ningun viaje)
	 * @throws IllegalStateException si el cliente ya tiene un viaje activo o le falta calificar su ultimo viaje
	 */
	public SolicitudViaje pedirViaje(Usuario cliente, Servicio servicio, Ubicacion origen, Ubicacion destino) {

		if (cliente.getCliente().viajePendienteDeCalificacion().isPresent()) {

			throw new IllegalStateException(Mensajes.get("cliente.calificacionPendiente"));
		}

		Optional<Asignacion> asignacion = Movilidad.seleccionarConductor(conductoresSimulados, servicio, origen);

		if (asignacion.isEmpty()) {

			Viaje rechazado = new Viaje(cliente, origen, destino, servicio);
			rechazado.solicitar(LocalDateTime.now());
			rechazarViaje(rechazado);
			return new SolicitudViaje(Resultado.SIN_CONDUCTORES, rechazado, null, null);
		}

		// se planifica antes de pedir el viaje, asi un error vial no deja nada a medias
		Vehiculo vehiculo = asignacion.get().vehiculo();
		RutasViaje rutas = new PlanificacionViajeService(motorVial).planificar(vehiculo, origen, destino);

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());

		if (!solicitarViaje(viaje)) {

			viaje.cancelar(LocalDateTime.now(), cliente, "Sin capacidad");
			return new SolicitudViaje(Resultado.COLA_LLENA, viaje, null, null);
		}

		return new SolicitudViaje(Resultado.ACEPTADA, viaje, rutas, vehiculo);
	}

	/**
	 * Productor: deja un viaje ya SOLICITADO en la cola para que lo tome un conductor simulado.
	 *
	 * @return false si la cola esta llena
	 */
	public boolean solicitarViaje(Viaje viaje) {

		return gestor.solicitar(viaje);
	}

	/**
	 * Rechaza de inmediato un viaje recien solicitado porque no hay ningun conductor disponible para el servicio.
	 *
	 * @throws IllegalStateException si el viaje no esta SOLICITADO
	 */
	public void rechazarViaje(Viaje viaje) {

		viaje.rechazar(LocalDateTime.now());
		viajeCambio(viaje);
	}

	/**
	 * Cancela un viaje a pedido de un usuario. Los hilos de los conductores notan el cambio de estado y dejan de
	 * simular ese viaje; el conductor queda DISPONIBLE.
	 *
	 * @throws IllegalStateException si el viaje ya termino
	 */
	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo) {

		viaje.cancelar(LocalDateTime.now(), usuario, motivo);
		viajeCambio(viaje);
	}

	/**
	 * Registra quien recibe los avisos de cambio de un viaje. Lo invocan los hilos de los conductores, asi que el
	 * observador debe pasar a su propio hilo de interfaz antes de tocar la pantalla.
	 */
	public void setObservadorViajes(Consumer<Viaje> observador) {

		this.observadorViajes = observador;
	}

	/** Detiene los hilos de simulacion (se llama al cerrar la aplicacion). */
	public void detener() {

		gestor.detener();
	}

	private void viajeCambio(Viaje viaje) {

		// al terminar el viaje el vehiculo queda en el destino: desde ahi sale el proximo
		if (viaje.estadoActual() == EstadoViaje.FINALIZADO && viaje.getVehiculo() != null
				&& viaje.getDestino() != null) {

			viaje.getVehiculo().setUbicacion(viaje.getDestino());
		}

		Consumer<Viaje> observador = observadorViajes;

		if (observador != null) {

			observador.accept(viaje);
		}
	}

	/** Detiene los hilos y cierra el grafo del motor de rutas. */
	@Override
	public void close() {

		gestor.detener();
		motorVial.close();
	}
}
