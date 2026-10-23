/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory de servicios.
 * Formato: nombre;tarifaBase;precioKm;precioMinuto;tipoVehiculo;categoriaVehiculo;tipoServicio
 */
package datos.factory;

import java.util.ArrayList;

import servicio.Servicio;
import servicio.TipoServicio;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;

public class ServicioSubFabrica implements SubFabrica<ArrayList<Servicio>> {

	@Override
	public Lector<ArrayList<Servicio>> crearLector() {

		return new LectorServicios();
	}

	private static class LectorServicios extends LectorBase<ArrayList<Servicio>> {

		@Override
		protected ArrayList<Servicio> crearResultado() {

			return new ArrayList<>();
		}

		@Override
		protected void procesarLinea(String[] partes, ArrayList<Servicio> servicios) {

			if (partes.length < 7) {

				return;
			}

			String nombre = partes[0].trim();
			double tarifaBase = Double.parseDouble(partes[1].trim());
			double precioKm = Double.parseDouble(partes[2].trim());
			double precioMinuto = Double.parseDouble(partes[3].trim());
			TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[4].trim());
			CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(partes[5].trim());
			TipoServicio tipoServicio = TipoServicio.valueOf(partes[6].trim());

			servicios.add(new Servicio(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo, categoria,
					tipoServicio));
		}
	}
}
