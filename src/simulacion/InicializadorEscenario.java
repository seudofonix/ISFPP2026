/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Prepara el estado inicial de los conductores ya cargados: los pone en servicio (DISPONIBLE) y ubica cada
 * vehiculo sobre una calle. No simula viajes ni decide quien presta un servicio (eso es de Movilidad).
 */
package simulacion;

import java.util.Collection;

import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.Vehiculo;

public final class InicializadorEscenario {

	private InicializadorEscenario() {
	}

	public static void inicializar(Collection<Usuario> usuarios, GeneradorUbicacionesViales generador) {

		for (Usuario usuario : usuarios) {

			if (usuario.getConductor() == null) {

				continue;
			}

			for (Vehiculo vehiculo : usuario.getConductor().getVehiculos().values()) {

				vehiculo.setUbicacion(generador.generar());
			}

			if (usuario.getRolActivo() != RolUsuario.CONDUCTOR
					&& usuario.getConductor().getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO) {

				usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
			}
		}
	}
}
