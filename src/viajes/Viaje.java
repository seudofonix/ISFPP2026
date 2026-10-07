/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define un viaje, sus participantes, ubicaciones, servicio e historial de estados.
 */
package viajes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.RolUsuario;
import usuario.Usuario;
import usuario.Cliente;
import usuario.Conductor;
import vehiculo.Vehiculo;


public class Viaje {

	private UUID id;
	private List<RegistroViaje> registroViaje;

	private Usuario cliente;
	private Usuario conductor;

	private Ubicacion origen;
	private Ubicacion destino;

	private Vehiculo vehiculo;
	
	private CalificacionViaje calificacionConductor = CalificacionViaje.NO_CALIFICADO;
	private CalificacionViaje calificacionCliente = CalificacionViaje.NO_CALIFICADO;

	private RolUsuario rolCancela;
	private String motivoCancelacion;

	private Servicio servicio;

	/**
	 * Crea un viaje pendiente de inicializar con su identificador y datos operativos.
	 */
	public Viaje() {
		this.id = UUID.randomUUID();
		this.registroViaje = new ArrayList<>();
		this.calificacionConductor = CalificacionViaje.NO_CALIFICADO;
		this.calificacionCliente = CalificacionViaje.NO_CALIFICADO;
		registroViaje = new ArrayList<RegistroViaje>();
	}

	/**
	 * Crea un viaje con los participantes y ubicaciones iniciales.
	 *
	 * @param cliente usuario que solicita el servicio
	 * @param origen ubicación de partida
	 * @param destino ubicación de llegada
	 * @param servicio servicio de transporte seleccionado
	 */
	public Viaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		this.cliente = cliente;
		this.origen = origen;
		this.destino = destino;
		this.servicio = servicio;
		registroViaje = new ArrayList<RegistroViaje>();
	}

	/**
	 * Registra la solicitud del viaje.
	 *
	 * @param fechaHora fecha y hora en que se solicita el viaje
	 */
	public void solicitar(LocalDateTime fechaHora) {
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.SOLICITADO));
		cliente.getCliente().agregarViaje(this);
		// TODO: 
	}

	/**
	 * Registra la aceptación del viaje por un conductor.
	 *
	 * @param fechaHora fecha y hora en que se acepta el viaje
	 * @param conductor conductor que acepta el viaje
	 */
	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		this.conductor = conductor;
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.ACEPTADO));
		
	}

	/**
	 * Registra el inicio del viaje.
	 *
	 * @param fechaHora fecha y hora en que se inicia el viaje
	 */
	public void iniciar(LocalDateTime fechaHora) {
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.INICIADO));
	}

	/**
	 * Registra la finalización del viaje y asigna las calificaciones correspondientes.
	 *
	 * @param fechaHora fecha y hora en que finaliza el viaje
	 * @param calificacionConductor calificación otorgada al conductor
	 * @param calificacionCliente calificación otorgada al cliente
	 */
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor, CalificacionViaje calificacionCliente) {
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.FINALIZADO));
		this.calificacionConductor = calificacionConductor;
		this.calificacionCliente = calificacionCliente;
	}

	/**
	 * Registra la finalización del viaje y recibe una única calificación común.
	 *
	 * @param fechaHora fecha y hora en que finaliza el viaje
	 * @param calificacion calificación común otorgada a ambos participantes
	 */
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacion) {
		finalizar(fechaHora, calificacion, calificacion);
	}

	/**
	 * Registra la cancelación del viaje y conserva su motivo y el rol del participante que cancela.
	 *
	 * @param fechaHora fecha y hora en que se cancela el viaje
	 * @param usuario usuario que realiza la cancelación
	 * @param motivo motivo informado para la cancelación
	 */
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.CANCELADO));
		this.motivoCancelacion = motivo;
		if (usuario != null) {
			if (usuario.equals(this.cliente)) {
				this.rolCancela = RolUsuario.CLIENTE;
			} else if (usuario.equals(this.conductor)) {
				this.rolCancela = RolUsuario.CONDUCTOR;
			} else {
				this.rolCancela = usuario.getRolActivo();
			}
		}
	}

	/**
	 * Registra el rechazo del viaje.
	 *
	 * @param fechaHora fecha y hora en que se rechaza el viaje
	 */
	public void rechazar(LocalDateTime fechaHora) {
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.RECHAZADO));
	}

	/**
	 * Obtiene el último estado registrado para el viaje.
	 *
	 * @return estado actual del viaje, o null si no posee registros
	 */
	public EstadoViaje estadoActual() {
		if (registroViaje == null || registroViaje.isEmpty()) {
			return null;
		}
		return registroViaje.getLast().getEstadoViaje();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public List<RegistroViaje> getRegistroViaje() {
		return new ArrayList<>(registroViaje);
	}

	public void setRegistroViaje(List<RegistroViaje> registroViaje) {
		this.registroViaje = registroViaje != null ? new ArrayList<>(registroViaje) : new ArrayList<>();
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public Usuario getConductor() {
		return conductor;
	}

	public void setConductor(Usuario conductor) {
		this.conductor = conductor;
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public void setOrigen(Ubicacion origen) {
		this.origen = origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public void setDestino(Ubicacion destino) {
		this.destino = destino;
	}
	
	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public CalificacionViaje getCalificacionConductor() {
		return calificacionConductor;
	}

	public void setCalificacionConductor(CalificacionViaje calificacionConductor) {
		this.calificacionConductor = calificacionConductor;
	}

	public CalificacionViaje getCalificacionCliente() {
		return calificacionCliente;
	}

	public void setCalificacionCliente(CalificacionViaje calificacionCliente) {
		this.calificacionCliente = calificacionCliente;
	}

	public RolUsuario getRolCancela() {
		return rolCancela;
	}

	public void setRolCancela(RolUsuario rolCancela) {
		this.rolCancela = rolCancela;
	}

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}
	
	

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Viaje viaje = (Viaje) o;
		return Objects.equals(id, viaje.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Viaje{" +
				"id=" + id +
				", cliente=" + (cliente != null ? cliente.getNombre() : "null") +
				", conductor=" + (conductor != null ? conductor.getNombre() : "null") +
				", origen=" + origen +
				", destino=" + destino +
				", servicio=" + (servicio != null ? servicio.getNombre() : "null") +
				", estadoActual=" + estadoActual() +
				", calificacionConductor=" + calificacionConductor +
				", calificacionCliente=" + calificacionCliente +
				", rolCancela=" + rolCancela +
				", motivoCancelacion='" + motivoCancelacion + '\'' +
				", registroViaje=" + registroViaje +
				'}';
	}
}
