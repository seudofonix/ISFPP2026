/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Producto del patron Factory: sabe leer un archivo y devolver el resultado ya armado.
 */
package datos.factory;

import java.io.IOException;

public interface Lector<T> {

	T leer(String nombreArchivo) throws IOException;
}
