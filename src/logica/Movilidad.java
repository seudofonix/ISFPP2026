/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Reglas del dominio para elegir quien presta un servicio: conductores compatibles y disponibles, el mas
 * cercano al origen segun Haversine, y clientes que pueden pedir un viaje. No pertenecen al inicializador
 * del escenario ni a la interfaz.
 */
package logica;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.Conductor;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.Vehiculo;

public final class Movilidad {

	private Movilidad() {
	}

	/**
	 * Elige el conductor DISPONIBLE, con rol conductor, que tenga un vehiculo compatible con el servicio y este
	 * mas cerca del origen (distancia en linea recta). Los empates se resuelven por email.
	 *
	 * @return la asignacion, o vacio si no hay ningun conductor disponible para ese servicio
	 */
	public static Optional<Asignacion> seleccionarConductor(Collection<Usuario> usuarios, Servicio servicio,
			Ubicacion origen) {

		if (usuarios == null || servicio == null || origen == null) {

			return Optional.empty();
		}

		return usuarios.stream().map(u -> candidato(u, servicio, origen)).flatMap(Optional::stream)
				.min(Comparator.comparingDouble(Asignacion::distanciaKm)
						.thenComparing(a -> a.conductor().getEmail()));
	}

	/** Usuarios que pueden pedir un viaje: los que no tienen uno activo como clientes. */
	public static List<Usuario> filtrarClientesDisponibles(Collection<Usuario> usuarios) {

		return usuarios.stream().filter(u -> u.getCliente() != null && !u.getCliente().enViaje()).toList();
	}

	private static Optional<Asignacion> candidato(Usuario usuario, Servicio servicio, Ubicacion origen) {

		Conductor conductor = usuario.getConductor();

		if (conductor == null || usuario.getRolActivo() != RolUsuario.CONDUCTOR
				|| conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {

			return Optional.empty();
		}

		Vehiculo vehiculo = conductor.vehiculoCompatible(servicio);

		if (vehiculo == null || vehiculo.getUbicacion() == null) {

			return Optional.empty();
		}

		return Optional.of(new Asignacion(usuario, vehiculo, origen.calcularDistancia(vehiculo.getUbicacion())));
	}
}
