/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Registra un cambio de estado de un viaje junto con el momento en que ocurrió.
 */
package viajes;

import java.time.*;

public class RegistroViaje {
	
	LocalDateTime fechaHora;
	private EstadoViaje estadoViaje;
	
	/**
	 * Crea un registro con el estado indicado y la fecha y hora actuales.
	 *
	 * @param estado estado del viaje que se registra
	 */
	RegistroViaje(EstadoViaje estado) {
		estadoViaje = estado;
		fechaHora = LocalDateTime.now();
	}
	
	/**
	 * Obtiene la fecha y hora en que se registró el cambio de estado.
	 *
	 * @return fecha y hora del registro
	 */
	public LocalDateTime getFechaHora() {
		return fechaHora;
	}
	/**
	 * Actualiza la fecha y hora del registro.
	 *
	 * @param fechaHora nueva fecha y hora del registro
	 */
	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}
	/**
	 * Obtiene el estado de viaje asociado al registro.
	 *
	 * @return estado de viaje registrado
	 */
	public EstadoViaje getEstadoViaje() {
		return estadoViaje;
	}
	/**
	 * Actualiza el estado de viaje asociado al registro.
	 *
	 * @param estadoViaje nuevo estado de viaje
	 */
	public void setEstadoViaje(EstadoViaje estadoViaje) {
		this.estadoViaje = estadoViaje;
	}

	// TODO: Incorporar hashCode y equals.
}
