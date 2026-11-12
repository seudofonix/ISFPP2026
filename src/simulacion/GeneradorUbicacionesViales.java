/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Genera posiciones aleatorias dentro del area de simulacion y verifica que caigan cerca de una calle
 * transitable (las ajusta al punto de la calle). Se usa para el boton "Aleatorio" y para ubicar a los
 * conductores al iniciar.
 */
package simulacion;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import datos.CargarParametros;
import rutas.api.PlanificadorRutas;
import ubicacion.Ubicacion;

public class GeneradorUbicacionesViales {

	private static final int MAX_INTENTOS = 200;

	private final PlanificadorRutas motor;
	private final Random azar;

	public GeneradorUbicacionesViales(PlanificadorRutas motor) {

		this(motor, new Random());
	}

	public GeneradorUbicacionesViales(PlanificadorRutas motor, Random azar) {

		this.motor = Objects.requireNonNull(motor, "motor");
		this.azar = Objects.requireNonNull(azar, "azar");
	}

	/**
	 * @return un punto sobre una calle dentro del area
	 * @throws IllegalStateException si despues de varios intentos no encontro ninguno
	 */
	public Ubicacion generar() {

		for (int i = 0; i < MAX_INTENTOS; i++) {

			Ubicacion candidata = new Ubicacion(entre(CargarParametros.getLatitud1(), CargarParametros.getLatitud2()),
					entre(CargarParametros.getLongitud1(), CargarParametros.getLongitud2()));

			Optional<Ubicacion> enCalle = motor.ajustarACalle(candidata, CargarParametros.getToleranciaAjusteMetros());

			if (enCalle.isPresent()) {

				return enCalle.get();
			}
		}

		throw new IllegalStateException("No se pudo generar una ubicacion sobre una calle dentro del area de "
				+ "simulacion. Revisa latitud1/latitud2/longitud1/longitud2 y el mapa de rutas.osm.");
	}

	private double entre(double a, double b) {

		double min = Math.min(a, b);
		double max = Math.max(a, b);

		return min + azar.nextDouble() * (max - min);
	}
}
