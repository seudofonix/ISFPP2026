/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Esqueleto comun de los lectores de base de datos (Template Method). Ejecuta la consulta de cada
 * subclase y le pasa cada fila. El parametro de leer() existe solo por la interfaz Lector y se ignora.
 */
package datos.factory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import datos.ConexionBD;

public abstract class LectorBD<T> implements Lector<T> {

	@Override
	public final T leer(String ignorado) throws IOException {

		T resultado = crearResultado();

		try {
			Connection conexion = ConexionBD.obtenerConexion();

			try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(consulta())) {

				while (rs.next()) {

					procesarFila(rs, resultado);
				}
			}

		} catch (SQLException | RuntimeException e) {

			throw new IOException("Error leyendo de la base de datos: " + e.getMessage(), e);
		}

		return resultado;
	}

	protected abstract String consulta();

	protected abstract T crearResultado();

	protected abstract void procesarFila(ResultSet rs, T resultado) throws SQLException;
}
