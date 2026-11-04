/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de vehiculos desde la base de datos (tabla vehiculo: enums como enteros y tipo_servicios
 * como mascara de bits).
 */
package datos.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;

public class VehiculoBDSubFabrica implements SubFabrica<HashMap<String, Vehiculo>> {

	static final String CONSULTA = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, "
			+ "categoria_vehiculo, tipo_servicios FROM vehiculo ORDER BY patente";

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

				vehiculos.put(patente,
						new Vehiculo(patente, rs.getString("modelo").trim(), rs.getInt("capacidad_pasajeros"),
								CodigosBD.enumPorCodigo(TipoVehiculo.values(), rs.getInt("tipo_vehiculo"),
										"tipo_vehiculo"),
								CodigosBD.enumPorCodigo(CategoriaVehiculo.values(), rs.getInt("categoria_vehiculo"),
										"categoria_vehiculo"),
								CodigosBD.serviciosDesdeMascara(rs.getInt("tipo_servicios"))));
			}
		};
	}
}
