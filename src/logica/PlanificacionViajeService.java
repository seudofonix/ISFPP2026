/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Servicio de aplicacion: compone las dos consultas viales de un viaje (vehiculo hacia el origen y origen
 * hacia el destino) usando el contrato PlanificadorRutas. No dibuja ni cotiza.
 */
package logica;

import java.util.Objects;

import rutas.api.PlanificadorRutas;
import rutas.api.RutasViaje;
import ubicacion.Ubicacion;
import vehiculo.Vehiculo;

public class PlanificacionViajeService {

	private final PlanificadorRutas planificador;

	public PlanificacionViajeService(PlanificadorRutas planificador) {

		this.planificador = Objects.requireNonNull(planificador, "planificador");
	}

	/**
	 * @throws rutas.api.RutaNoDisponibleException si alguno de los dos tramos no tiene recorrido
	 */
	public RutasViaje planificar(Vehiculo vehiculo, Ubicacion origen, Ubicacion destino) {

		Objects.requireNonNull(vehiculo, "vehiculo");

		if (vehiculo.getUbicacion() == null) {

			throw new IllegalStateException("El vehiculo " + vehiculo.getPatente() + " no tiene ubicacion.");
		}

		var haciaOrigen = planificador.calcular(vehiculo.getUbicacion(), origen, vehiculo.getTipoVehiculo());
		var haciaDestino = planificador.calcular(origen, destino, vehiculo.getTipoVehiculo());

		return new RutasViaje(haciaOrigen, haciaDestino);
	}
}
