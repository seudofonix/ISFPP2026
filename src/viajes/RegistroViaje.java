/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Registra un cambio de estado de un viaje junto con el momento en que ocurrió.
 */
package viajes;

import java.time.*;
import java.util.Objects;

public class RegistroViaje {
	
	private LocalDateTime fechaHora;
	private EstadoViaje estadoViaje;
	
	/**
	 * Crea un registro con la fecha, hora y estado indicados.
	 *
	 * @param fechaHora fecha y hora en que se registra el evento
	 * @param estado estado del viaje que se registra
	 */
	public RegistroViaje(LocalDateTime fechaHora, EstadoViaje estado) {
		this.estadoViaje = estado;
		this.fechaHora = (fechaHora != null) ? fechaHora : LocalDateTime.now();
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

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		RegistroViaje that = (RegistroViaje) o;
		return Objects.equals(fechaHora, that.fechaHora) && estadoViaje == that.estadoViaje;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fechaHora, estadoViaje);
	}

	@Override
	public String toString() {
		return "RegistroViaje{" +
				"fechaHora=" + fechaHora +
				", estadoViaje=" + estadoViaje +
				'}';
	}
}
