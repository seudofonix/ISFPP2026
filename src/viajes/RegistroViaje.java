package viajes;

// Cada instancia de registroViaje almacena un cambio en el estado de la solicitud. 
// EJ. Si la solicitud pasa de SOLICITADA a ACEPTADA se crea una instancia que registra
// la fecha y hora en la que se acepto. 

import java.time.*;

public class RegistroViaje {
	
	LocalDateTime fechaHora;
	private EstadoViaje estadoViaje;
	
	RegistroViaje(EstadoViaje estado) {
		estadoViaje = estado;
		fechaHora = LocalDateTime.now();
	}
	
	public LocalDateTime getFechaHora() {
		return fechaHora;
	}
	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}
	public EstadoViaje getEstadoViaje() {
		return estadoViaje;
	}
	public void setEstadoViaje(EstadoViaje estadoViaje) {
		this.estadoViaje = estadoViaje;
	}
	
		
}
