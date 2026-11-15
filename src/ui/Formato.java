/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Formato de datos de viajes para mostrar en cualquier interfaz (JavaFX o consola): estrellas de la calificacion,
 * fecha del viaje y fila del registro de viajes de un conductor.
 */
package ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import aplicacion.ContextoAplicacion;
import idioma.Mensajes;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.RegistroViaje;
import viajes.Viaje;

public final class Formato {

	private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private Formato() {
	}

	/** Una fila del registro de viajes (todo ya convertido a texto, en el idioma actual). */
	public record FilaRegistro(String id, String cliente, String servicio, String origen, String destino,
			String estado, String fecha, String calificacion) {
	}

	/** "★★★★☆" para 4 de 5; "-" si todavia no se califico. */
	public static String estrellas(CalificacionViaje calificacion) {

		if (calificacion == null || calificacion == CalificacionViaje.NO_CALIFICADO) {

			return "-";
		}

		int valor = calificacion.getValor();
		return "★".repeat(valor) + "☆".repeat(5 - valor);
	}

	/** Texto de la calificacion con su nombre en el idioma actual: "★★★★☆ Muy bueno". */
	public static String calificacionConNombre(CalificacionViaje calificacion) {

		return estrellas(calificacion) + " " + Mensajes.get("calificacion." + calificacion.name());
	}

	/** Fecha y hora en que se solicito el viaje. */
	public static String fecha(Viaje viaje) {

		List<RegistroViaje> registros = viaje.getRegistroViaje();
		return registros.isEmpty() ? "-" : registros.get(0).getFechaHora().format(FECHA_HORA);
	}

	/** Registro de viajes de un conductor, del mas nuevo al mas viejo. */
	public static List<FilaRegistro> registroDeConductor(ContextoAplicacion contexto, List<Viaje> viajes) {

		return viajes.stream().sorted((a, b) -> fecha(b).compareTo(fecha(a))).map(v -> new FilaRegistro(
				v.getId().toString().substring(0, 8), v.getCliente().getNombre(), v.getServicio().getNombre(),
				contexto.getGeocodificador().direccion(v.getOrigen()),
				contexto.getGeocodificador().direccion(v.getDestino()),
				Mensajes.get("estado." + v.estadoActual().name()), fecha(v),
				estrellas(v.getCalificacionConductor()))).toList();
	}

	/** Promedio de las calificaciones recibidas, o -1 si ninguno de los viajes fue calificado. */
	public static double promedio(List<Viaje> viajes) {

		return viajes.stream().filter(v -> v.estadoActual() == EstadoViaje.FINALIZADO)
				.map(Viaje::getCalificacionConductor).filter(c -> c != null && c != CalificacionViaje.NO_CALIFICADO)
				.mapToInt(CalificacionViaje::getValor).average().orElse(-1);
	}
}
