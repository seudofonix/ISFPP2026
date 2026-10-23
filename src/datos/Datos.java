/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga los datos del sistema desde archivos de texto. No parsea nada por si misma: pide a la
 * FabricaLectores la sub-factory de cada archivo, crea el lector y le delega la lectura.
 */
package datos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import datos.factory.FabricaLectores;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public class Datos {

	/**
	 * Carga los vehiculos desde el archivo de texto (campos separados con ;, el encabezado arranca con #).
	 *
	 * @return mapa indexado por patente
	 */
	public static HashMap<String, Vehiculo> cargarVehiculos(String nombreArchivo) throws IOException {

		return FabricaLectores.vehiculos().crearLector().leer(nombreArchivo);
	}

	/**
	 * Carga los usuarios desde el archivo de texto. Si el usuario trae licencia y patentes, tambien es
	 * conductor.
	 *
	 * @param vehiculos mapa de vehiculos ya cargados
	 * @return mapa indexado por email
	 */
	public static HashMap<String, Usuario> cargarUsuarios(String nombreArchivo, HashMap<String, Vehiculo> vehiculos)
			throws IOException {

		return FabricaLectores.usuarios(vehiculos).crearLector().leer(nombreArchivo);
	}

	/**
	 * Carga los servicios desde el archivo de texto.
	 *
	 * @return lista de servicios
	 */
	public static ArrayList<Servicio> cargarServicios(String nombreArchivo) throws IOException {

		return FabricaLectores.servicios().crearLector().leer(nombreArchivo);
	}
}
