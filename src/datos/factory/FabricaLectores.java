/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Factory raiz: entrega la sub-factory correspondiente a cada tipo de archivo de datos.
 */
package datos.factory;

import java.util.HashMap;
import java.util.Map;

import vehiculo.Vehiculo;

public final class FabricaLectores {

	private FabricaLectores() {
	}

	public static VehiculoSubFabrica vehiculos() {

		return new VehiculoSubFabrica();
	}

	public static UsuarioSubFabrica usuarios(Map<String, Vehiculo> vehiculos) {

		return new UsuarioSubFabrica(vehiculos != null ? vehiculos : new HashMap<>());
	}

	public static ServicioSubFabrica servicios() {

		return new ServicioSubFabrica();
	}
}
