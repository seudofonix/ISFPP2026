/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de servicios desde la base de datos.
 */
package datos.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import servicio.Servicio;
import servicio.TipoServicio;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;

public class ServicioBDSubFabrica implements SubFabrica<ArrayList<Servicio>> {

	// AJUSTAR a los nombres reales de tablas y columnas
	static final String CONSULTA = "SELECT nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, "
			+ "categoria_vehiculo, tipo_servicio FROM servicio ORDER BY nombre";

	@Override
	public Lector<ArrayList<Servicio>> crearLector() {

		return new LectorBD<ArrayList<Servicio>>() {

			@Override
			protected String consulta() {
				return CONSULTA;
			}

			@Override
			protected ArrayList<Servicio> crearResultado() {
				return new ArrayList<>();
			}

			@Override
			protected void procesarFila(ResultSet rs, ArrayList<Servicio> servicios) throws SQLException {

				servicios.add(new Servicio(rs.getString("nombre").trim(), rs.getDouble("tarifa_base"),
						rs.getDouble("precio_km"), rs.getDouble("precio_minuto"),
						TipoVehiculo.valueOf(rs.getString("tipo_vehiculo").trim()),
						CategoriaVehiculo.valueOf(rs.getString("categoria_vehiculo").trim()),
						TipoServicio.valueOf(rs.getString("tipo_servicio").trim())));
			}
		};
	}
}
