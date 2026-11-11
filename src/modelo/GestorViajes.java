/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * GESTOR DE VIAJES. El cliente (productor) deja los viajes en una cola compartida. Cada
 * conductor simulado (consumidor) es un hilo que espera, toma un viaje, lo acepta y simula su
 * recorrido. Cuando cambia un viaje, avisa al Modelo para que se refresque la pantalla.
 */
package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import usuario.Conductor;
import usuario.EstadoConductor;
import usuario.Usuario;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.Viaje;

public class GestorViajes {

	private static final int CAPACIDAD_COLA = 50;
	private static final int PASOS_POR_TRAMO = 10;
	private static final int MAX_INTENTOS = 10;
	private static final long ESPERA_DEVOLUCION_MS = 300;
	private static final double SEGUNDOS_TRAMO = 4.0;

	private final BlockingQueue<Viaje> solicitudes = new LinkedBlockingQueue<>(CAPACIDAD_COLA);
	private final Map<Viaje, Integer> intentos = new ConcurrentHashMap<>();
	private final List<Thread> hilos = new ArrayList<>();
	private final List<Usuario> conductoresActivos = new ArrayList<>();
	private final Consumer<Viaje> alCambiar;

	private volatile boolean activo;

	/**
	 * @param alCambiar aviso que recibe el Modelo cuando cambia un viaje
	 */
	public GestorViajes(Consumer<Viaje> alCambiar) {

		this.alCambiar = alCambiar;
	}

	/**
	 * Arranca un hilo por conductor simulado. Si no hay conductores, no arranca nada.
	 *
	 * @param conductores usuarios que ya tienen un perfil de conductor
	 */
	public void iniciar(List<Usuario> conductores) {

		if (activo) {

			return;
		}

		List<Usuario> validos = conductores.stream().filter(usuario -> usuario.getConductor() != null).toList();

		if (validos.isEmpty()) {

			return;
		}

		activo = true;

		for (Usuario usuario : validos) {

			usuario.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
			conductoresActivos.add(usuario);

			Thread hilo = new Thread(() -> atender(usuario), "conductor-" + usuario.getNombre());
			hilo.setDaemon(true);
			hilos.add(hilo);
			hilo.start();
		}
	}

	/**
	 * Productor: deja un viaje en la cola compartida. Lo llama el hilo de JavaFX.
	 *
	 * @param viaje viaje recien solicitado
	 * @return false si la cola esta llena
	 */
	public boolean solicitar(Viaje viaje) {

		return solicitudes.offer(viaje);
	}

	/**
	 * Consumidor: cada conductor espera su proximo viaje, lo acepta y simula su ciclo de vida.
	 *
	 * @param usuario conductor que atiende la cola
	 */
	private void atender(Usuario usuario) {

		while (activo) {

			try {

				Viaje viaje = solicitudes.take();

				if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {

					intentos.remove(viaje);
					continue;
				}

				Conductor conductor = usuario.getConductor();

				if (conductor == null || conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {

					devolver(viaje);
					continue;
				}

				try {

					viaje.aceptar(LocalDateTime.now(), usuario);

				} catch (IllegalStateException ex) {

					// No es compatible (por ejemplo, por la categoria del vehiculo) o el viaje ya no esta SOLICITADO.
					devolver(viaje);
					continue;
				}

				intentos.remove(viaje);
				avisar(viaje);

				try {

					simular(viaje);

				} finally {

					EstadoViaje estado = viaje.estadoActual();

					if (!activo && (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO)) {

						viaje.cancelar(LocalDateTime.now(), null, "Simulacion detenida");
						avisar(viaje);
					}
				}

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/**
	 * Devuelve el viaje a la cola para que lo intente otro conductor. Cuenta los intentos: cuando
	 * se agotan, el viaje queda RECHAZADO. La pausa evita que el mismo conductor lo tome
	 * inmediatamente en un bucle.
	 */
	private void devolver(Viaje viaje) throws InterruptedException {

		if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {

			intentos.remove(viaje);
			return;
		}

		int numero = intentos.merge(viaje, 1, Integer::sum);

		if (numero >= MAX_INTENTOS) {

			rechazar(viaje);
			return;
		}

		if (!solicitudes.offer(viaje)) {

			rechazar(viaje);
			return;
		}
		Thread.sleep(ESPERA_DEVOLUCION_MS);
	}

	/**
	 * Marca el viaje como RECHAZADO cuando ningun conductor puede tomarlo. Se usa cuando se
	 * agotan los intentos o cuando la cola esta llena.
	 */
	private void rechazar(Viaje viaje) {

		intentos.remove(viaje);

		try {

			if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {

				viaje.rechazar(LocalDateTime.now());
				avisar(viaje);
			}

		} catch (IllegalStateException ex) {

			// El cliente u otro hilo lo cancelo antes.
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

	/**
	 * Duerme el tramo en varios pasos, pero corta si el viaje dejo el estado esperado o si el hilo
	 * fue interrumpido.
	 *
	 * @return true si el viaje siguio en el estado esperado durante todo el tramo
	 */
	private boolean avanzar(Viaje viaje, EstadoViaje esperado) {

		for (int paso = 1; paso <= PASOS_POR_TRAMO; paso++) {

			if (viaje.estadoActual() != esperado) {

				return false;
			}

			try {

				Thread.sleep((long) (SEGUNDOS_TRAMO * 1000 / PASOS_POR_TRAMO));

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}

	/** Avisa al Modelo que cambio un viaje, sin cortar el hilo del conductor si la pantalla falla. */
	private void avisar(Viaje viaje) {

		try {

			alCambiar.accept(viaje);

		} catch (RuntimeException ex) {

			// Si la pantalla falla, el conductor sigue funcionando.
		}
	}

	/**
	 * Detiene los hilos, cancela los viajes que quedaron en curso o pendientes y devuelve los
	 * conductores simulados a fuera de servicio.
	 */
	public void detener() {

		activo = false;
		for (Thread hilo : hilos) {

			hilo.interrupt();
		}
		for (Thread hilo : hilos) {

			esperarHilo(hilo);
		}
		hilos.clear();

		cancelarPendientes();
		intentos.clear();

		for (Usuario usuario : conductoresActivos) {

			if (usuario.getConductor() != null) {

				usuario.getConductor().setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
			}
		}
		conductoresActivos.clear();
	}

	/**
	 * Espera a que el hilo termine, aunque interrumpan al hilo actual. No usa un tiempo maximo de
	 * espera porque los hilos de los conductores solo esperan en take() o sleep(), y se pueden
	 * despertar con interrupt().
	 */
	private void esperarHilo(Thread hilo) {

		boolean interrumpido = false;

		while (hilo.isAlive()) {

			try {

				hilo.join();

			} catch (InterruptedException ex) {

				interrumpido = true;
			}
		}

		if (interrumpido) {

			Thread.currentThread().interrupt();
		}
	}

	/** Cancela los viajes que quedaron esperando en la cola al detener la simulacion. */
	private void cancelarPendientes() {

		Viaje pendiente;

		while ((pendiente = solicitudes.poll()) != null) {

			try {

				if (pendiente.estadoActual() == EstadoViaje.SOLICITADO) {

					pendiente.cancelar(LocalDateTime.now(), null, "Simulacion detenida");
					avisar(pendiente);
				}

			} catch (IllegalStateException ex) {

				// Ya lo habia cancelado otro hilo.
			}
			intentos.remove(pendiente);
		}
	}

	/**
	 * Descarta las solicitudes pendientes. Se usa cuando se recargan los datos desde archivo o base.
	 */
	public void limpiar() {

		solicitudes.clear();
		intentos.clear();
	}
}
