/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Consumidor de la simulacion: un hilo por conductor. Espera un viaje en su bandeja, lo acepta y
 * simula sus dos tramos con el mismo hilo.
 */
package simulacion;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import usuario.Usuario;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.Viaje;

public final class ConsumidorConductor implements Runnable {

	private final Usuario conductor;
	private final BandejaViajes bandeja;
	private final ConfiguracionSimulacion configuracion;
	private final DuracionTramo duracion;
	private final Consumer<Viaje> alCambiar;
	private final AtomicBoolean activo;

	public ConsumidorConductor(Usuario conductor, BandejaViajes bandeja, ConfiguracionSimulacion configuracion,
			DuracionTramo duracion, Consumer<Viaje> alCambiar, AtomicBoolean activo) {

		this.conductor = conductor;
		this.bandeja = bandeja;
		this.configuracion = configuracion;
		this.duracion = duracion;
		this.alCambiar = alCambiar;
		this.activo = activo;
	}

	@Override
	public void run() {

		Thread.currentThread().setName("conductor-" + conductor.getNombre());

		while (activo.get()) {

			Viaje viaje = null;

			try {

				viaje = bandeja.tomar(conductor);

				if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {

					continue;
				}

				viaje.aceptar(LocalDateTime.now(), conductor);
				bandeja.retirar(viaje);
				avisar(viaje);
				simular(viaje);

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
				return;

			} catch (IllegalStateException ex) {

				// Otro conductor lo acepto primero o el viaje ya no se puede aceptar.

			} catch (RuntimeException ex) {

				// Un error inesperado no debe matar al conductor ni dejar el viaje colgado.
				System.err.println("Error en la simulacion del viaje: " + ex.getMessage());
				cancelarPorError(viaje, conductor);

			} finally {

				if (!activo.get()) {

					cancelarSiSigueEnCurso(viaje, conductor);
				}
			}
		}
	}

	/**
	 * Simula los dos tramos del viaje con el mismo hilo del conductor:
	 * ACEPTADO (llega al origen) -> INICIADO (va al destino) -> FINALIZADO.
	 */
	private void simular(Viaje viaje) {

		if (!avanzar(viaje, EstadoViaje.ACEPTADO)) {

			return;
		}

		synchronized (viaje) {

			if (viaje.estadoActual() != EstadoViaje.ACEPTADO) {

				return;
			}
			viaje.iniciar(LocalDateTime.now());
		}
		avisar(viaje);

		if (!avanzar(viaje, EstadoViaje.INICIADO)) {

			return;
		}

		synchronized (viaje) {

			if (viaje.estadoActual() != EstadoViaje.INICIADO) {

				return;
			}
			viaje.finalizar(LocalDateTime.now(), CalificacionViaje.NO_CALIFICADO);
		}
		avisar(viaje);
	}

	private boolean avanzar(Viaje viaje, EstadoViaje esperado) {

		double segundos = segundosDelTramo(viaje, esperado);
		long pasoMs = Math.max(1, (long) (segundos * 1000 / configuracion.pasosTramo()));

		for (int paso = 1; paso <= configuracion.pasosTramo(); paso++) {

			if (viaje.estadoActual() != esperado) {

				return false;
			}

			try {

				Thread.sleep(pasoMs);

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}

	/** Si el calculo del tramo falla (por ejemplo, por una ruta no disponible) se usa el tiempo base. */
	private double segundosDelTramo(Viaje viaje, EstadoViaje tramo) {

		try {

			return Math.max(0, duracion.segundos(viaje, tramo));

		} catch (RuntimeException ex) {

			return configuracion.segundosTramo();
		}
	}

	/** Avisa al Modelo que cambio un viaje, sin cortar el hilo del conductor si la pantalla falla. */
	private void avisar(Viaje viaje) {

		try {

			alCambiar.accept(viaje);

		} catch (RuntimeException ex) {

			// Si la pantalla falla, el conductor sigue funcionando.
		}
	}

	/** Si el conductor habia aceptado el viaje, lo cancela porque se detuvo la simulacion. */
	private void cancelarSiSigueEnCurso(Viaje viaje, Usuario conductor) {

		if (viaje == null || viaje.getConductor() == null || !viaje.getConductor().equals(conductor)) {

			return;
		}

		boolean cancelado = false;

		synchronized (viaje) {

			EstadoViaje estado = viaje.estadoActual();

			if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {

				try {

					viaje.cancelar(LocalDateTime.now(), null, "Simulacion detenida");
					cancelado = true;

				} catch (IllegalStateException ex) {

					// Ya lo habia cancelado otro hilo.
				}
			}
		}

		if (cancelado) {

			avisar(viaje);
		}
	}

	/** Cancela el viaje si un error inesperado corto su simulacion. */
	private void cancelarPorError(Viaje viaje, Usuario conductor) {

		if (viaje == null || viaje.getConductor() == null || !viaje.getConductor().equals(conductor)) {

			return;
		}

		boolean cancelado = false;

		synchronized (viaje) {

			EstadoViaje estado = viaje.estadoActual();

			if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {

				try {

					viaje.cancelar(LocalDateTime.now(), null, "Error en la simulacion");
					cancelado = true;

				} catch (IllegalStateException ex) {

					// Ya lo habia cancelado otro hilo.
				}
			}
		}

		if (cancelado) {

			avisar(viaje);
		}
	}
}
