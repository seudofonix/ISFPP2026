/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Esqueleto comun de todos los lectores (Template Method): abre el archivo, ignora lineas vacias y
 * comentarios (#), separa los campos por ';' y delega en cada subclase como interpretar una linea.
 */
package datos.factory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public abstract class LectorBase<T> implements Lector<T> {

	private static final String SEPARADOR = ";";

	@Override
	public final T leer(String nombreArchivo) throws IOException {

		T resultado = crearResultado();

		try (Scanner entrada = new Scanner(new File(nombreArchivo))) {

			int numeroLinea = 0;

			while (entrada.hasNextLine()) {

				numeroLinea++;
				String linea = entrada.nextLine().trim();

				if (linea.isEmpty() || linea.startsWith("#")) {

					continue;
				}

				String[] partes = linea.split(SEPARADOR);

				try {

					procesarLinea(partes, resultado);

				} catch (IllegalArgumentException e) {

					throw new IllegalArgumentException(
							nombreArchivo + ", linea " + numeroLinea + ": " + e.getMessage(), e);
				}
			}
		}

		return resultado;
	}

	/** Crea el contenedor vacio donde se iran acumulando las entidades leidas. */
	protected abstract T crearResultado();

	/** Interpreta una linea ya separada en campos y la agrega al resultado. */
	protected abstract void procesarLinea(String[] partes, T resultado);
}
