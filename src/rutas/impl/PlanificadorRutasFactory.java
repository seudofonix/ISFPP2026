/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Fabrica que elige que implementacion de PlanificadorRutas crear segun 'rutas.motor' de config.properties.
 * Para agregar otro motor: nueva clase en este paquete + una rama en crear().
 */
package rutas.impl;

import datos.CargarParametros;
import rutas.api.PlanificadorRutas;
import rutas.api.RutaNoDisponibleException;

public final class PlanificadorRutasFactory {

	private PlanificadorRutasFactory() {
	}

	public static PlanificadorRutas crear() {

		String motor = CargarParametros.getRutasMotor();

		if ("graphhopper".equals(motor)) {

			return new GraphHopperPlanificador(CargarParametros.getRutasOsm(), CargarParametros.getRutasCache());
		}

		throw new RutaNoDisponibleException("Motor de rutas desconocido: '" + motor + "'. Usar 'graphhopper'.");
	}
}
