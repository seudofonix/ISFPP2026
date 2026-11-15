/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * SIMULACION DE VIAJES. El productor deja una copia del viaje en la bandeja de cada conductor
 * compatible. Cada conductor simulado (consumidor) es un hilo que espera en su propia bandeja,
 * toma un viaje, lo acepta y simula su recorrido. El primero que lo acepta gana; si nadie lo
 * acepta antes del tiempo maximo, el viaje queda RECHAZADO.
 */
package simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import servicio.Servicio;
import usuario.Conductor;
import usuario.EstadoConductor;
import usuario.Usuario;
import viajes.EstadoViaje;
import viajes.Viaje;

public final class Simulacion {

	private static final long ESPERA_CIERRE_MS = 5000;

	private final Consumer<Viaje> alCambiar;
	private final GeneradorUbicacionesViales generador;
	private final BandejaViajes bandejas = new BandejaViajes();
	private final List<Usuario> conductoresActivos = new ArrayList<>();
	private final AtomicBoolean activo = new AtomicBoolean(false);
	private final AtomicInteger viajesSolicitados = new AtomicInteger();
	private final ReentrantLock candadoCiclo = new ReentrantLock();

	private ExecutorService consumidores;
	private ScheduledExecutorService programador;
	private Thread hiloProductor;
	private ConfiguracionSimulacion configuracion;

	/**
	 * @param alCambiar aviso que recibe el Modelo cuando cambia un viaje
	 * @param generador generador de ubicaciones usado por el productor automatico
	 */
	public Simulacion(Consumer<Viaje> alCambiar, GeneradorUbicacionesViales generador) {

		this.alCambiar = Objects.requireNonNull(alCambiar, "alCambiar");
		this.generador = generador;
	}

	/**
	 * Inicia la simulacion sin productor automatico. Se usa en las pruebas y en los casos en que
	 * solo interesa la demanda manual de la interfaz.
	 */
	public void iniciar(List<Usuario> conductores, ConfiguracionSimulacion configuracion) {

		iniciar(conductores, List.of(), List.of(), configuracion);
	}

	/**
	 * Arranca un hilo por conductor simulado y, si la configuracion lo indica, el hilo productor.
	 *
	 * @param conductores   usuarios que ya tienen un perfil de conductor
	 * @param clientes      usuarios que puede usar el productor automatico
	 * @param servicios     servicios disponibles para generar viajes
	 * @param configuracion tiempos, capacidad, duracion y maximo de conductores
	 */
	public void iniciar(List<Usuario> conductores, List<Usuario> clientes, List<Servicio> servicios,
			ConfiguracionSimulacion configuracion) {

		if (conductores == null || configuracion == null) {

			throw new IllegalArgumentException("Faltan los conductores o la configuracion de la simulacion.");
		}

		if (activo.get()) {

			return;
		}

		List<Usuario> validos = conductores.stream()
				.filter(usuario -> usuario != null && usuario.getConductor() != null).toList();

		if (validos.isEmpty()) {

			return;
		}

		candadoCiclo.lock();

		try {

			if (activo.get()) {

				return;
			}

			this.configuracion = configuracion;
			bandejas.reiniciar();
			conductoresActivos.clear();

			for (Usuario usuario : validos) {

				usuario.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
				bandejas.registrar(usuario, configuracion.capacidadBandeja());
				conductoresActivos.add(usuario);
			}

			activo.set(true);

			consumidores = Executors.newFixedThreadPool(validos.size(), tarea -> {

				Thread hilo = new Thread(tarea, "conductor-simulado");
				hilo.setDaemon(true);
				return hilo;
			});

			for (Usuario usuario : validos) {

				consumidores.submit(
						new ConsumidorConductor(usuario, bandejas, configuracion, this::avisar, activo));
			}

			programador = Executors.newSingleThreadScheduledExecutor(tarea -> {

				Thread hilo = new Thread(tarea, "expirador-viajes");
				hilo.setDaemon(true);
				return hilo;
			});

			if (configuracion.productorAutomatico()) {

				iniciarProductor(clientes, servicios, configuracion);
			}

			programarDuracion(configuracion.duracionSegundos());

		} finally {

			candadoCiclo.unlock();
		}
	}

	private void iniciarProductor(List<Usuario> clientes, List<Servicio> servicios,
			ConfiguracionSimulacion configuracion) {

		if (generador == null || servicios == null || servicios.isEmpty()) {

			return;
		}

		ProductorViajes productor = new ProductorViajes(clientes, servicios, generador, configuracion, activo,
				this::solicitar);
		hiloProductor = new Thread(productor, "productor-viajes");
		hiloProductor.setDaemon(true);
		hiloProductor.start();
	}

	private void programarDuracion(double duracionSegundos) {

		if (duracionSegundos <= 0 || programador == null) {

			return;
		}

		long duracionMs = Math.max(1, (long) (duracionSegundos * 1000));

		programador.schedule(() -> {

			Thread hilo = new Thread(this::detener, "detener-por-duracion");
			hilo.setDaemon(true);
			hilo.start();

		}, duracionMs, TimeUnit.MILLISECONDS);
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

		if (bandejas.conductores().isEmpty()) {

			// La simulacion no esta iniciada (por ejemplo, en pruebas del Modelo).
			return true;
		}

		List<Usuario> compatibles = bandejas.conductores().stream()
				.filter(usuario -> esCompatible(usuario, viaje)).toList();

		if (compatibles.isEmpty()) {

			programarExpiracion(viaje);
			viajesSolicitados.incrementAndGet();
			return true;
		}

		boolean encolado = bandejas.ofrecer(viaje, compatibles);

		if (!encolado) {

			return false;
		}

		programarExpiracion(viaje);
		viajesSolicitados.incrementAndGet();
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

		return conductor.vehiculoCompatible(servicio) != null;
	}

	/** Deja programado el rechazo del viaje para cuando se cumpla el tiempo maximo de espera. */
	private void programarExpiracion(Viaje viaje) {

		ScheduledExecutorService ejecutor = programador;

		if (!activo.get() || ejecutor == null || ejecutor.isShutdown() || configuracion == null) {

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

			bandejas.retirar(viaje);
			avisar(viaje);
		}
	}

	/**
	 * Detiene los hilos, cancela los viajes que quedaron en curso o pendientes y devuelve los
	 * conductores simulados a fuera de servicio.
	 */
	public void detener() {

		if (!candadoCiclo.tryLock()) {

			return;
		}

		try {

			activo.set(false);

			Thread productorHilo = hiloProductor;
			hiloProductor = null;

			if (productorHilo != null) {

				productorHilo.interrupt();
				esperarHilo(productorHilo);
			}

			apagar(programador);
			programador = null;

			apagar(consumidores);
			consumidores = null;

			cancelarPendientes();

			for (Usuario usuario : conductoresActivos) {

				if (usuario.getConductor() != null) {

					usuario.getConductor().setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
				}
			}

			conductoresActivos.clear();
			bandejas.reiniciar();
			configuracion = null;

		} finally {

			candadoCiclo.unlock();
		}
	}

	/** Vacia las solicitudes pendientes sin detener los hilos. Se usa al recargar los datos. */
	public void limpiar() {

		bandejas.limpiar();
	}

	public boolean estaActiva() {

		return activo.get();
	}

	public int getViajesSolicitados() {

		return viajesSolicitados.get();
	}

	/** Cancela los viajes que quedaron esperando en las bandejas al detener la simulacion. */
	private void cancelarPendientes() {

		for (Viaje viaje : bandejas.drenar()) {

			cancelarPendiente(viaje);
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

	/** Avisa al Modelo que cambio un viaje, sin cortar el hilo del conductor si la pantalla falla. */
	private void avisar(Viaje viaje) {

		try {

			alCambiar.accept(viaje);

		} catch (RuntimeException ex) {

			// Si la pantalla falla, el conductor sigue funcionando.
		}
	}

	private void apagar(ExecutorService ejecutor) {

		if (ejecutor == null) {

			return;
		}

		ejecutor.shutdownNow();

		try {

			if (!ejecutor.awaitTermination(ESPERA_CIERRE_MS, TimeUnit.MILLISECONDS)) {

				System.err.println("La simulacion no termino todos sus hilos a tiempo.");
			}

		} catch (InterruptedException ex) {

			Thread.currentThread().interrupt();
		}
	}

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
}
