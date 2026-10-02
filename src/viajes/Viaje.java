package viajes;

import java.util.UUID;
import java.time.*;
import java.util.List;

import servicio.*;
import ubicacion.*;
import usuario.*;
import vehiculo.*;
import viajes.EstadoViaje;

/**
 * Instancia Supervisada de Formacion Practica Profesional (ISFPP) Ignacio Bullon, 2026. UNPSJB.
 *
 * Representa el recorrido contratado, con sus participantes, vehículo, servicio y ubicaciones.
 */
public class Viaje {
	
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
	
	Viaje() {
		// TODO: Inicializar el UUID.
	}
	
	public void solicitar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.SOLICITADO ) );
	}
	
	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.ACEPTADO ) );
	}
	
	public void iniciar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.INICIADO ) );
	}
	
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje Calificacion) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.FINALIZADO ) );
	}
	
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.CANCELADO ) );
		motivoCancelacion = motivo;
	}
	
	public void rechazar(LocalDateTime fechaHora) {
		registroViaje.addLast( new RegistroViaje( EstadoViaje.RECHAZADO ) );
	}
	
	public EstadoViaje estadoActual() {
		
		 // Devolver el ultimo cambio de estado registrado.  
		 return registroViaje.getLast().getEstadoViaje();
	}
}
