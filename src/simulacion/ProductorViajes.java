/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Productor de la simulacion: genera viajes cada cierto intervalo usando clientes y servicios
 * reales (o clientes simulados si no hay disponibles) y los publica en la simulacion.
 */
package simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.Usuario;
import viajes.EstadoViaje;
import viajes.Viaje;

public final class ProductorViajes implements Runnable {

	private final List<Usuario> clientes;
	private final List<Servicio> servicios;
	private final GeneradorUbicacionesViales generador;
	private final ConfiguracionSimulacion configuracion;
	private final AtomicBoolean activo;
	private final Predicate<Viaje> publicar;
	private final AtomicInteger generados = new AtomicInteger();
	private final AtomicInteger sinteticos = new AtomicInteger();
	private final Random azar = new Random();

	public ProductorViajes(List<Usuario> clientes, List<Servicio> servicios, GeneradorUbicacionesViales generador,
			ConfiguracionSimulacion configuracion, AtomicBoolean activo, Predicate<Viaje> publicar) {

		this.clientes = clientes == null ? List.of() : new ArrayList<>(clientes);
		this.servicios = servicios == null ? List.of() : new ArrayList<>(servicios);
		this.generador = generador;
		this.configuracion = configuracion;
		this.activo = activo;
		this.publicar = publicar;
	}

	@Override
	public void run() {

		if (servicios.isEmpty() || generador == null) {

			return;
		}

		while (activo.get() && (configuracion.cantidadViajes() == 0
				|| generados.get() < configuracion.cantidadViajes())) {

			try {

				esperarIntervalo();

				if (!activo.get()) {

					return;
				}

				Viaje viaje = crearViaje();

				if (viaje == null) {

					continue;
				}

				if (publicar.test(viaje)) {

					generados.incrementAndGet();

				} else {

					cancelarSinCapacidad(viaje);
				}

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();
				return;

			} catch (RuntimeException ex) {

				System.err.println("Error al generar un viaje simulado: " + ex.getMessage());
			}
		}
	}

	private void esperarIntervalo() throws InterruptedException {

		long intervaloMs = Math.max(1, (long) (configuracion.intervaloViajesSegundos() * 1000));
		Thread.sleep(intervaloMs);
	}

	private Viaje crearViaje() {

		Usuario cliente = elegirCliente();

		if (cliente == null) {

			return null;
		}

		Servicio servicio = servicios.get(azar.nextInt(servicios.size()));
		Ubicacion origen = generador.generar();
		Ubicacion destino = generador.generar();

		if (origen.calcularDistancia(destino) <= 0) {

			return null;
		}

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);

		try {

			viaje.solicitar(LocalDateTime.now());

		} catch (IllegalStateException ex) {

			return null;
		}
		return viaje;
	}

	private Usuario elegirCliente() {

		List<Usuario> disponibles = clientes.stream()
				.filter(cliente -> cliente.getCliente() != null && !cliente.getCliente().enViaje()).toList();

		if (!disponibles.isEmpty()) {

			return disponibles.get(azar.nextInt(disponibles.size()));
		}

		return clienteSimulado();
	}

	/** Permite que la simulacion funcione aunque no haya clientes cargados en los datos. */
	private Usuario clienteSimulado() {

		int numero = sinteticos.incrementAndGet();

		return new Usuario("Cliente simulado " + numero, "000-" + numero, "simulado" + numero + "@simulacion.local");
	}

	private void cancelarSinCapacidad(Viaje viaje) {

		synchronized (viaje) {

			if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {

				try {

					viaje.cancelar(LocalDateTime.now(), viaje.getCliente(), "Sin capacidad en la simulacion");

				} catch (IllegalStateException ex) {

					// Otro hilo ya lo tomo o lo cancelo.
				}
			}
		}
	}
}
