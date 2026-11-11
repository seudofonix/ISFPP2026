/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * GESTOR DE VIAJES. El cliente (productor) deja una copia del viaje en la bandeja de cada
 * conductor compatible. Cada conductor simulado (consumidor) es un hilo que espera en su propia
 * bandeja, toma un viaje, lo acepta y simula su recorrido. El primero que lo acepta gana; si nadie
 * lo acepta antes del tiempo maximo, el viaje queda RECHAZADO.
 */
package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import servicio.Servicio;
import usuario.Conductor;
import usuario.EstadoConductor;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.Viaje;

public class GestorViajes {

	private static final long ESPERA_CIERRE_MS = 5000;

	private final List<Bandeja> bandejas = new ArrayList<>();
	private final List<Thread> hilos = new ArrayList<>();
	private final List<Usuario> conductoresActivos = new ArrayList<>();
	private final Consumer<Viaje> alCambiar;

	private ScheduledExecutorService expirador;
	private ConfiguracionSimulacion configuracion;
	private volatile boolean activo;

	/**
	 * @param alCambiar aviso que recibe el Modelo cuando cambia un viaje
	 */
	public GestorViajes(Consumer<Viaje> alCambiar) {

		this.alCambiar = alCambiar;
	}

	/**
	 * Arranca un hilo por conductor simulado. Cada conductor recibe su propia bandeja y todos los
	 * conductores compatibles reciben una copia del mismo viaje; el primero que lo acepta gana.
	 *
	 * @param conductores usuarios que ya tienen un perfil de conductor
	 * @param configuracion tiempos, capacidad y maximo de conductores de la simulacion
	 */
	public void iniciar(List<Usuario> conductores, ConfiguracionSimulacion configuracion) {

		if (activo) {

			return;
		}

		if (conductores == null || configuracion == null) {

			throw new IllegalArgumentException("Faltan los conductores o la configuracion de la simulacion.");
		}

		List<Usuario> validos = conductores.stream().filter(usuario -> usuario.getConductor() != null).toList();

		if (validos.isEmpty()) {

			return;
		}

		this.configuracion = configuracion;
		expirador = Executors.newSingleThreadScheduledExecutor(tarea -> {

			Thread hilo = new Thread(tarea, "expirador-viajes");
			hilo.setDaemon(true);
			return hilo;
		});
		activo = true;

		for (Usuario usuario : validos) {

			usuario.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
			Bandeja bandeja = new Bandeja(usuario, configuracion.capacidadBandeja());
			bandejas.add(bandeja);
			conductoresActivos.add(usuario);

			Thread hilo = new Thread(() -> atender(bandeja), "conductor-" + usuario.getNombre());
			hilo.setDaemon(true);
			hilos.add(hilo);
			hilo.start();
		}
	}

	/**
	 * Productor: ofrece el viaje a todos los conductores compatibles. Si ninguno lo puede tomar,
	 * el viaje queda esperando y el temporizador lo rechaza cuando se cumple el tiempo maximo.
	 *
	 * @param viaje viaje recien solicitado
	 * @return false solo si todas las bandejas compatibles estan llenas
	 */
	public boolean solicitar(Viaje viaje) {

		if (viaje == null) {

			throw new IllegalArgumentException("El viaje no puede ser nulo.");
		}

		if (bandejas.isEmpty()) {

			// La simulacion no esta iniciada (por ejemplo, en pruebas del Modelo).
			return true;
		}

		boolean hayCompatible = false;
		boolean encolado = false;

		for (Bandeja bandeja : bandejas) {

			if (esCompatible(bandeja.conductor, viaje)) {

				hayCompatible = true;
				encolado |= bandeja.viajes.offer(viaje);
			}
		}

		if (hayCompatible && !encolado) {

			return false;
		}

		programarExpiracion(viaje);

		return true;
	}

	/**
	 * Consumidor: espera un viaje en su bandeja, lo acepta y simula su ciclo de vida.
	 *
	 * @param bandeja bandeja del conductor que atiende este hilo
	 */
	private void atender(Bandeja bandeja) {

		while (activo) {

			Viaje viaje = null;

			try {

				viaje = bandeja.viajes.take();

				if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {

					continue;
				}

				viaje.aceptar(LocalDateTime.now(), bandeja.conductor);
				quitarDeBandejas(viaje);
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
				cancelarPorError(viaje, bandeja.conductor);

			} finally {

				if (!activo) {

					cancelarSiSigueEnCurso(viaje, bandeja.conductor);
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

		long pasoMs = Math.max(1,
				(long) (configuracion.segundosTramo() * 1000 / configuracion.pasosTramo()));

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

	/** Un conductor solo recibe viajes de una categoria igual o mayor y del tipo de servicio pedido. */
	private boolean esCompatible(Usuario usuario, Viaje viaje) {

		Conductor conductor = usuario.getConductor();

		if (conductor == null || conductor.getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO) {

			return false;
		}

		Servicio servicio = viaje.getServicio();

		if (servicio == null) {

			return true;
		}

		for (Vehiculo vehiculo : conductor.getVehiculos().values()) {

			if (vehiculo.getCategoriaVehiculo() != null && servicio.getCategoriaVehiculo() != null
					&& vehiculo.getCategoriaVehiculo().ordinal() >= servicio.getCategoriaVehiculo().ordinal()
					&& (servicio.getTipoServicio() == null
							|| vehiculo.getTipoServicio().contains(servicio.getTipoServicio()))) {

				return true;
			}
		}
		return false;
	}

	/** Deja programado el rechazo del viaje para cuando se cumpla el tiempo maximo de espera. */
	private void programarExpiracion(Viaje viaje) {

		ScheduledExecutorService ejecutor = expirador;

		if (ejecutor == null || ejecutor.isShutdown() || configuracion == null) {

			return;
		}

		long esperaMs = (long) (configuracion.esperaMaximaSegundos() * 1000);
		ejecutor.schedule(() -> expirar(viaje), esperaMs, TimeUnit.MILLISECONDS);
	}

	/** Rechaza el viaje si todavia nadie lo acepto cuando se cumple el tiempo maximo. */
	private void expirar(Viaje viaje) {

		boolean rechazado = false;

		synchronized (viaje) {

			if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {

				try {

					viaje.rechazar(LocalDateTime.now());
					rechazado = true;

				} catch (IllegalStateException ex) {

					// Otro hilo lo acepto justo antes.
				}
			}
		}

		if (rechazado) {

			quitarDeBandejas(viaje);
			avisar(viaje);
		}
	}

	/**
	 * Detiene los hilos, cancela los viajes que quedaron en curso o pendientes y devuelve los
	 * conductores simulados a fuera de servicio.
	 */
	public void detener() {

		activo = false;

		if (expirador != null) {

			expirador.shutdownNow();

			try {

				expirador.awaitTermination(ESPERA_CIERRE_MS, TimeUnit.MILLISECONDS);

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
			}
			expirador = null;
		}

		for (Thread hilo : hilos) {

			hilo.interrupt();
		}
		for (Thread hilo : hilos) {

			esperarHilo(hilo);
		}
		hilos.clear();

		cancelarPendientes();
		bandejas.clear();

		for (Usuario usuario : conductoresActivos) {

			if (usuario.getConductor() != null) {

				usuario.getConductor().setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
			}
		}
		conductoresActivos.clear();
		configuracion = null;
	}

	/**
	 * Espera a que el hilo termine, pero no para siempre: si un conductor no responde en unos
	 * segundos, se avisa por consola y se sigue cerrando la aplicacion.
	 */
	private void esperarHilo(Thread hilo) {

		try {

			hilo.join(ESPERA_CIERRE_MS);

		} catch (InterruptedException ex) {

			Thread.currentThread().interrupt();
			return;
		}

		if (hilo.isAlive()) {

			System.err.println("El hilo " + hilo.getName() + " no termino a tiempo.");
		}
	}

	/** Cancela los viajes que quedaron esperando en las bandejas al detener la simulacion. */
	private void cancelarPendientes() {

		for (Bandeja bandeja : List.copyOf(bandejas)) {

			Viaje pendiente;

			while ((pendiente = bandeja.viajes.poll()) != null) {

				cancelarPendiente(pendiente);
			}
		}
	}

	private void cancelarPendiente(Viaje viaje) {

		boolean cancelado = false;

		synchronized (viaje) {

			if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {

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

	/** Saca el viaje de las bandejas de los conductores que todavia no lo tomaron. */
	private void quitarDeBandejas(Viaje viaje) {

		for (Bandeja bandeja : List.copyOf(bandejas)) {

			bandeja.viajes.remove(viaje);
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

	/** Descarta las solicitudes pendientes. Se usa cuando se recargan los datos desde archivo o base. */
	public void limpiar() {

		for (Bandeja bandeja : List.copyOf(bandejas)) {

			bandeja.viajes.clear();
		}
	}

	/** Bandeja de viajes de un conductor simulado. Cada hilo tiene la suya. */
	private static final class Bandeja {

		private final Usuario conductor;
		private final BlockingQueue<Viaje> viajes;

		private Bandeja(Usuario conductor, int capacidad) {

			this.conductor = conductor;
			this.viajes = new LinkedBlockingQueue<>(capacidad);
		}
	}
}
