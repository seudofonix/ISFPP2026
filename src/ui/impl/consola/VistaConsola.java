/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * VISTA de la interfaz de consola: lo unico que sabe es escribir texto y leer lineas. No conoce el dominio ni
 * las reglas; las decisiones las toma ControladorConsola. Recibe los flujos por constructor, asi se puede probar
 * con entradas y salidas en memoria.
 */
package ui.impl.consola;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VistaConsola {

	private final BufferedReader entrada;
	private final PrintStream salida;

	public VistaConsola(InputStream entrada, PrintStream salida) {

		this.entrada = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
		this.salida = salida;
	}

	public void mostrar(String texto) {

		salida.println(texto);
	}

	public void mostrarLineaEnBlanco() {

		salida.println();
	}

	/**
	 * Muestra la pregunta y espera una linea.
	 *
	 * @return la linea sin espacios en los extremos, o null si se cerro la entrada
	 */
	public String leer(String pregunta) {

		salida.print(pregunta + " ");
		salida.flush();

		try {

			String linea = entrada.readLine();
			return linea == null ? null : linea.trim();

		} catch (IOException e) {

			throw new UncheckedIOException(e);
		}
	}

	/** Hay una linea ya escrita esperando (sirve para atender un pedido del usuario sin bloquear). */
	public boolean hayEntrada() {

		try {

			return entrada.ready();

		} catch (IOException e) {

			throw new UncheckedIOException(e);
		}
	}

	/** Dibuja una tabla de texto con columnas alineadas. */
	public void mostrarTabla(List<String> encabezados, List<List<String>> filas) {

		int[] anchos = new int[encabezados.size()];

		for (int i = 0; i < anchos.length; i++) {

			anchos[i] = encabezados.get(i).length();

			for (List<String> fila : filas) {

				anchos[i] = Math.max(anchos[i], fila.get(i).length());
			}
		}

		salida.println(linea(encabezados, anchos));
		salida.println("-".repeat(linea(encabezados, anchos).length()));

		for (List<String> fila : filas) {

			salida.println(linea(fila, anchos));
		}
	}

	private String linea(List<String> celdas, int[] anchos) {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < anchos.length; i++) {

			sb.append(String.format("%-" + anchos[i] + "s", celdas.get(i)));
			sb.append(i < anchos.length - 1 ? " | " : "");
		}

		return sb.toString();
	}
}
