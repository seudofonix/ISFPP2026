/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de usuarios. Formato: nombre;telefono;email[;licencia;patente1;patente2...]
 * Si el usuario trae licencia y al menos una patente conocida, tambien es conductor.
 */
package datos.factory;

import java.util.HashMap;
import java.util.Map;

import usuario.Usuario;
import vehiculo.Vehiculo;

public class UsuarioSubFabrica implements SubFabrica<HashMap<String, Usuario>> {

	private final Map<String, Vehiculo> vehiculos;

	/**
	 * @param vehiculos vehiculos ya cargados, indexados por patente (los conductores se enlazan contra ellos)
	 */
	public UsuarioSubFabrica(Map<String, Vehiculo> vehiculos) {

		this.vehiculos = vehiculos;
	}

	@Override
	public Lector<HashMap<String, Usuario>> crearLector() {

		return new LectorUsuarios(vehiculos);
	}

	private static class LectorUsuarios extends LectorBase<HashMap<String, Usuario>> {

		private final Map<String, Vehiculo> vehiculos;

		LectorUsuarios(Map<String, Vehiculo> vehiculos) {

			this.vehiculos = vehiculos;
		}

		@Override
		protected HashMap<String, Usuario> crearResultado() {

			return new HashMap<>();
		}

		@Override
		protected void procesarLinea(String[] partes, HashMap<String, Usuario> usuarios) {

			if (partes.length < 3) {

				return;
			}

			String nombre = partes[0].trim();
			String telefono = partes[1].trim();
			String email = partes[2].trim();

			Usuario usuario = new Usuario(nombre, telefono, email);

			if (partes.length > 4 && !partes[3].isBlank()) {

				altaConductor(usuario, partes);
			}

			usuarios.put(email, usuario);
		}

		private void altaConductor(Usuario usuario, String[] partes) {

			String licencia = partes[3].trim();
			boolean esConductor = false;

			for (int i = 4; i < partes.length; i++) {

				String patente = partes[i].trim();

				if (patente.isBlank()) {

					continue;
				}

				Vehiculo vehiculo = vehiculos.get(patente);

				if (vehiculo == null) {

					continue;
				}

				if (!esConductor) {

					usuario.altaConductor(licencia, vehiculo);
					esConductor = true;

				} else {

					usuario.getConductor().agregarVehiculo(vehiculo);
				}
			}
		}
	}
}
