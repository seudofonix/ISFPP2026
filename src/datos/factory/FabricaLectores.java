/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Factory raiz: segun el origen configurado en config.properties (origen=archivo | bd) entrega la
 * sub-factory que lee de archivos de texto o de la base de datos.
 */
package datos.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import datos.CargarParametros;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public final class FabricaLectores {

	private FabricaLectores() {
	}

	private static boolean usaBD() {

		return "bd".equals(CargarParametros.getOrigen());
	}

	public static SubFabrica<HashMap<String, Vehiculo>> vehiculos() {

		return usaBD() ? new VehiculoBDSubFabrica() : new VehiculoSubFabrica();
	}

	public static SubFabrica<HashMap<String, Usuario>> usuarios(Map<String, Vehiculo> vehiculos) {

		Map<String, Vehiculo> v = vehiculos != null ? vehiculos : new HashMap<>();

		return usaBD() ? new UsuarioBDSubFabrica(v) : new UsuarioSubFabrica(v);
	}

	public static SubFabrica<ArrayList<Servicio>> servicios() {

		return usaBD() ? new ServicioBDSubFabrica() : new ServicioSubFabrica();
	}
}
