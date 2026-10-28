/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de usuarios desde la base de datos. Una fila por usuario y por vehiculo de conductor;
 * si el usuario tiene licencia y al menos un vehiculo conocido, tambien es conductor.
 */
package datos.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import usuario.Usuario;
import vehiculo.Vehiculo;

public class UsuarioBDSubFabrica implements SubFabrica<HashMap<String, Usuario>> {

	// AJUSTAR a los nombres reales de tablas y columnas
	static final String CONSULTA = "SELECT u.email, u.nombre, u.telefono, c.licencia_conducir, v.patente "
			+ "FROM usuario u LEFT JOIN conductor c ON c.email = u.email "
			+ "LEFT JOIN vehiculo v ON v.email_conductor = c.email " + "ORDER BY u.email, v.patente";

	private final Map<String, Vehiculo> vehiculos;

	public UsuarioBDSubFabrica(Map<String, Vehiculo> vehiculos) {

		this.vehiculos = vehiculos;
	}

	@Override
	public Lector<HashMap<String, Usuario>> crearLector() {

		return new LectorBD<HashMap<String, Usuario>>() {

			@Override
			protected String consulta() {
				return CONSULTA;
			}

			@Override
			protected HashMap<String, Usuario> crearResultado() {
				return new HashMap<>();
			}

			@Override
			protected void procesarFila(ResultSet rs, HashMap<String, Usuario> usuarios) throws SQLException {

				String email = rs.getString("email").trim();
				Usuario usuario = usuarios.get(email);

				if (usuario == null) {

					usuario = new Usuario(rs.getString("nombre").trim(), rs.getString("telefono").trim(), email);
					usuarios.put(email, usuario);
				}

				String licencia = rs.getString("licencia_conducir");
				String patente = rs.getString("patente");

				if (licencia == null || licencia.isBlank() || patente == null) {

					return;
				}

				Vehiculo vehiculo = vehiculos.get(patente.trim());

				if (vehiculo == null) {

					return;
				}

				if (usuario.getConductor() == null) {

					usuario.altaConductor(licencia.trim(), vehiculo);

				} else {

					usuario.getConductor().agregarVehiculo(vehiculo);
				}
			}
		};
	}
}
