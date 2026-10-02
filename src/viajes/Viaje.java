/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define un viaje, sus participantes, ubicaciones, servicio e historial de estados.
 */
package viajes;

import java.util.UUID;
import java.time.*;
import java.util.List;

import servicio.*;
import ubicacion.*;
import usuario.*;
import vehiculo.*;
import viajes.EstadoViaje;

public class Viaje {
	
	/**
	 * Enumera las calificaciones que pueden asignarse a los participantes de un viaje.
	 */
	private enum CalificacionViaje {
		NO_CALIFICADO,
		MALO,
		REGULAR,
		BUENO,
		MUY_BUENO,
		EXCELENTE
	}
	
	private UUID id;
	private List<RegistroViaje> registroViaje;
	
	private Usuario cliente;
	private Usuario conductor;
	
	private Ubicacion origen;
	private Ubicacion destino;
	
	private CalificacionViaje calificacionConductor = CalificacionViaje.NO_CALIFICADO;
	private CalificacionViaje calificacionCliente = CalificacionViaje.NO_CALIFICADO;
	
	private RolUsuario rolCancela;
	private String motivoCancelacion;
	
	private Servicio servicio;
	
	/**
	 * Crea un viaje pendiente de inicializar con su identificador y datos operativos.
	 */
	Viaje() {
		// TODO: Inicializar el UUID.
	}
	
	/**
	 * Registra la solicitud del viaje.
	 *
	 * @param fechaHora fecha y hora en que se solicita el viaje
	 */
	public void solicitar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.SOLICITADO ) );
	}
	
	/**
	 * Registra la aceptación del viaje por un conductor.
	 *
	 * @param fechaHora fecha y hora en que se acepta el viaje
	 * @param conductor conductor que acepta el viaje
	 */
	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.ACEPTADO ) );
	}
	
	/**
	 * Registra el inicio del viaje.
	 *
	 * @param fechaHora fecha y hora en que se inicia el viaje
	 */
	public void iniciar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.INICIADO ) );
	}
	
	/**
	 * Registra la finalización del viaje y recibe la calificación correspondiente.
	 *
	 * @param fechaHora fecha y hora en que finaliza el viaje
	 * @param Calificacion calificación asociada a la finalización
	 */
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje Calificacion) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.FINALIZADO ) );
	}
	
	/**
	 * Registra la cancelación del viaje y conserva su motivo.
	 *
	 * @param fechaHora fecha y hora en que se cancela el viaje
	 * @param usuario usuario que realiza la cancelación
	 * @param motivo motivo informado para la cancelación
	 */
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.CANCELADO ) );
		motivoCancelacion = motivo;
	}
	
	/**
	 * Registra el rechazo del viaje.
	 *
	 * @param fechaHora fecha y hora en que se rechaza el viaje
	 */
	public void rechazar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.RECHAZADO ) );
	}
	
	/**
	 * Obtiene el último estado registrado para el viaje.
	 *
	 * @return estado actual del viaje
	 */
	public EstadoViaje estadoActual() {
		
		 // Devolver el ultimo cambio de estado registrado.  
		 return registroViaje.getLast().getEstadoViaje();
	}

	// TODO: Incorporar getters, setters, hashCode y equals.
}
