/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Contrato del modulo de rutas: que se puede pedir, sin decidir como se resuelve. El resto de la aplicacion
 * depende de esta interfaz y nunca de la biblioteca concreta (GraphHopper).
 */
package rutas.api;

import java.util.Optional;

import ubicacion.Ubicacion;
import vehiculo.TipoVehiculo;

public interface PlanificadorRutas extends AutoCloseable {

	/**
	 * Calcula el recorrido vial entre dos puntos.
	 *
	 * @throws RutaNoDisponibleException si no hay camino o el perfil del vehiculo no esta implementado
	 */
	RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo);

	/**
	 * Ajusta una coordenada a la calle transitable mas proxima (snapping).
	 *
	 * @return la posicion sobre la calle, o vacio si no hay ninguna dentro de la distancia maxima
	 */
	Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros);

	/**
	 * Nombre de la calle sobre la que esta (o a la que se ajusta) un punto.
	 *
	 * @return el nombre, o vacio si la calle no tiene nombre o el motor no lo informa
	 */
	default Optional<String> nombreCalle(Ubicacion punto) {

		return Optional.empty();
	}

	@Override
	void close();
}
