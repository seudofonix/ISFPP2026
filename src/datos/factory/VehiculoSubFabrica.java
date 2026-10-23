/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de vehiculos. Formato: patente;modelo;capacidad;tipo;categoria;servicio1;servicio2...
 */
package datos.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import servicio.TipoServicio;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;

public class VehiculoSubFabrica implements SubFabrica<HashMap<String, Vehiculo>> {

	@Override
	public Lector<HashMap<String, Vehiculo>> crearLector() {

		return new LectorVehiculos();
	}

	private static class LectorVehiculos extends LectorBase<HashMap<String, Vehiculo>> {

		@Override
		protected HashMap<String, Vehiculo> crearResultado() {

			return new HashMap<>();
		}

		@Override
		protected void procesarLinea(String[] partes, HashMap<String, Vehiculo> vehiculos) {

			if (partes.length < 5) {

				return;
			}

			String patente = partes[0].trim();
			String modelo = partes[1].trim();
			int capacidad = Integer.parseInt(partes[2].trim());
			TipoVehiculo tipo = TipoVehiculo.valueOf(partes[3].trim());
			CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(partes[4].trim());

			Set<TipoServicio> servicios = new HashSet<>();

			for (int i = 5; i < partes.length; i++) {

				String servicioStr = partes[i].trim();

				if (!servicioStr.isBlank()) {

					servicios.add(TipoServicio.valueOf(servicioStr));
				}
			}

			vehiculos.put(patente, new Vehiculo(patente, modelo, capacidad, tipo, categoria, servicios));
		}
	}
}
