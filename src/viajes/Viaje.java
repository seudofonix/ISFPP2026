package viajes;

import java.util.UUID;
import java.time.*;
import java.util.ArrayList;

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
	private ArrayList<RegistroViaje> registroViaje;
	
	private Usuario cliente;
	private Usuario conductor;
	
	private Ubicacion origen;
	private Ubicacion destino;
	
	private CalificacionViaje calificacionConductor = CalificacionViaje.NO_CALIFICADO;
	private CalificacionViaje calificacionCliente = CalificacionViaje.NO_CALIFICADO;
	
	private RolUsuario rolCancela;
	private String motivoCancelacion;
	
	Viaje() {
		// TODO: Inicializar el UUID.
	}
	
	public void solicitar(LocalDateTime fechaHora) {
		
	}
	
	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		
	}
	
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje Calificacion) {
		
	}
	
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		
	}
	
	public void rechazar(LocalDateTime fechaHora) {
		
	}
	
	public EstadoViaje estadoActual() {
		// TODO: Ver como se devuelve el estado del viaje.
	}
}
