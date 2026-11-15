/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Datos compartidos entre el productor y los consumidores: una cola bloqueante por conductor.
 * El productor ofrece el mismo viaje a todos los conductores compatibles y el primero que lo
 * acepta gana; los demas lo descartan cuando lo sacan de su cola.
 */
package simulacion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import usuario.Usuario;
import viajes.Viaje;

public final class BandejaViajes {

	private final List<Bandeja> bandejas = new CopyOnWriteArrayList<>();

	/**
	 * Registra la cola de un conductor simulado.
	 *
	 * @param conductor conductor que atendera su propia cola
	 * @param capacidad cantidad maxima de viajes pendientes
	 */
	public void registrar(Usuario conductor, int capacidad) {

		bandejas.add(new Bandeja(conductor, capacidad));
	}

	/**
	 * Deja una copia del viaje en la cola de cada conductor compatible.
	 *
	 * @return true si al menos una cola acepto el viaje
	 */
	public boolean ofrecer(Viaje viaje, Collection<Usuario> compatibles) {

		boolean encolado = false;

		for (Bandeja bandeja : bandejas) {

			if (compatibles.contains(bandeja.conductor)) {

				encolado |= bandeja.viajes.offer(viaje);
			}
		}
		return encolado;
	}

	/**
	 * Espera hasta que haya un viaje en la bandeja del conductor.
	 *
	 * @throws InterruptedException si el hilo es interrumpido mientras espera
	 */
	public Viaje tomar(Usuario conductor) throws InterruptedException {

		Bandeja bandeja = buscar(conductor);

		if (bandeja == null) {

			throw new IllegalStateException("El conductor no tiene una bandeja registrada.");
		}
		return bandeja.viajes.take();
	}

	/** Saca el viaje de todas las bandejas, para que ningun otro conductor lo intente. */
	public void retirar(Viaje viaje) {

		for (Bandeja bandeja : bandejas) {

			bandeja.viajes.remove(viaje);
		}
	}

	/** Vacia las colas sin desregistrar a los conductores. */
	public void limpiar() {

		for (Bandeja bandeja : bandejas) {

			bandeja.viajes.clear();
		}
	}

	/** Descarta las bandejas registradas. Se usa al terminar una simulacion completa. */
	public void reiniciar() {

		bandejas.clear();
	}

	/** Copia de los conductores registrados. */
	public List<Usuario> conductores() {

		return bandejas.stream().map(bandeja -> bandeja.conductor).toList();
	}

	/**
	 * Vacia todas las colas y devuelve los viajes pendientes sin repetir.
	 */
	public List<Viaje> drenar() {

		Set<Viaje> pendientes = new LinkedHashSet<>();

		for (Bandeja bandeja : bandejas) {

			Viaje viaje;

			while ((viaje = bandeja.viajes.poll()) != null) {

				pendientes.add(viaje);
			}
		}
		return new ArrayList<>(pendientes);
	}

	private Bandeja buscar(Usuario conductor) {

		for (Bandeja bandeja : bandejas) {

			if (bandeja.conductor.equals(conductor)) {

				return bandeja;
			}
		}
		return null;
	}

	private static final class Bandeja {

		private final Usuario conductor;
		private final BlockingQueue<Viaje> viajes;

		private Bandeja(Usuario conductor, int capacidad) {

			this.conductor = conductor;
			this.viajes = new LinkedBlockingQueue<>(capacidad);
		}
	}
}
