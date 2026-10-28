/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de vehiculos desde la base de datos. Una fila por vehiculo y por tipo de servicio.
 */
package datos.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import servicio.TipoServicio;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;

public class VehiculoBDSubFabrica implements SubFabrica<HashMap<String, Vehiculo>> {

	// AJUSTAR a los nombres reales de tablas y columnas
	static final String CONSULTA = "SELECT v.patente, v.modelo, v.capacidad_pasajeros, v.tipo_vehiculo, "
			+ "v.categoria_vehiculo, vs.tipo_servicio "
			+ "FROM vehiculo v LEFT JOIN vehiculo_servicio vs ON vs.patente = v.patente "
			+ "ORDER BY v.patente";

	@Override
	public Lector<HashMap<String, Vehiculo>> crearLector() {

		return new LectorBD<HashMap<String, Vehiculo>>() {

			@Override
			protected String consulta() {
				return CONSULTA;
			}

			@Override
			protected HashMap<String, Vehiculo> crearResultado() {
				return new HashMap<>();
			}

			@Override
			protected void procesarFila(ResultSet rs, HashMap<String, Vehiculo> vehiculos) throws SQLException {

				String patente = rs.getString("patente").trim();
				Vehiculo vehiculo = vehiculos.get(patente);

				if (vehiculo == null) {

					vehiculo = new Vehiculo(patente, rs.getString("modelo").trim(), rs.getInt("capacidad_pasajeros"),
							TipoVehiculo.valueOf(rs.getString("tipo_vehiculo").trim()),
							CategoriaVehiculo.valueOf(rs.getString("categoria_vehiculo").trim()), new HashSet<>());
					vehiculos.put(patente, vehiculo);
				}

				String servicio = rs.getString("tipo_servicio");

				if (servicio != null && !servicio.isBlank()) {

					vehiculo.agregarTipoServicio(TipoServicio.valueOf(servicio.trim()));
				}
			}
		};
	}
}
