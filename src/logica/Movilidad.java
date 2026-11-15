/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Reglas del dominio para elegir quien presta un servicio: conductores compatibles y disponibles, el mas
 * cercano al origen segun Haversine, y que conductores atienden la simulacion. No pertenecen al inicializador
 * del escenario ni a la interfaz.
 */
package logica;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	/**
	 * Elige los conductores que atienden la simulacion. Primero se toman los que cubren mas servicios todavia sin
	 * cubrir (un conductor con vehiculo PREMIUM tambien cubre CONFORT y ESTANDAR, pero no al reves) y, si sobran
	 * lugares, se completa por email. Asi cada servicio del catalogo tiene a alguien que pueda tomarlo aunque se
	 * simulen pocos conductores.
	 *
	 * @param excluido usuario de la sesion, que nunca se simula (puede ser null)
	 * @param maximo   cantidad maxima de conductores
	 */
	public static List<Usuario> elegirConductoresSimulados(Collection<Usuario> usuarios,
			Collection<Servicio> servicios, Usuario excluido, int maximo) {

		List<Usuario> pendientes = usuarios.stream()
				.filter(u -> u.getConductor() != null && !u.equals(excluido))
				.sorted(Comparator.comparing(Usuario::getEmail)).collect(Collectors.toCollection(ArrayList::new));
		List<Usuario> elegidos = new ArrayList<>();
		Set<Servicio> sinCubrir = new LinkedHashSet<>(servicios);

		while (elegidos.size() < maximo && !pendientes.isEmpty() && !sinCubrir.isEmpty()) {

			Usuario mejor = null;
			long mejorCobertura = 0;

			for (Usuario candidato : pendientes) {

				long cobertura = sinCubrir.stream()
						.filter(s -> candidato.getConductor().vehiculoCompatible(s) != null).count();

				if (cobertura > mejorCobertura) {

					mejor = candidato;
					mejorCobertura = cobertura;
				}
			}

			if (mejor == null) {

				break;
			}

			Usuario elegido = mejor;
			elegidos.add(elegido);
			pendientes.remove(elegido);
			sinCubrir.removeIf(s -> elegido.getConductor().vehiculoCompatible(s) != null);
		}

		for (Usuario usuario : pendientes) {

			if (elegidos.size() >= maximo) {

				break;
			}

			elegidos.add(usuario);
		}

		return List.copyOf(elegidos);
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
