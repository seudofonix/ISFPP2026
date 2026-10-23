/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * MODELO del patron MVC. Guarda los datos del sistema en colecciones observables de JavaFX, de modo que
 * la Vista se actualiza sola cuando cambian. No conoce ni la Vista ni el Controlador.
 */
package modelo;

import java.io.IOException;
import java.util.HashMap;

import datos.CargarParametros;
import datos.Datos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public class Modelo {

	private final ObservableList<Vehiculo> vehiculos = FXCollections.observableArrayList();
	private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
	private final ObservableList<Servicio> servicios = FXCollections.observableArrayList();

	public ObservableList<Vehiculo> getVehiculos() {

		return vehiculos;
	}

	public ObservableList<Usuario> getUsuarios() {

		return usuarios;
	}

	public ObservableList<Servicio> getServicios() {

		return servicios;
	}

	/**
	 * (Re)carga todos los datos desde los archivos indicados en config.properties, usando Datos (que a su
	 * vez usa las sub-factories de lectura).
	 */
	public void cargar() throws IOException {

		CargarParametros.parametros();

		HashMap<String, Vehiculo> mapaVehiculos = Datos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
		HashMap<String, Usuario> mapaUsuarios = Datos.cargarUsuarios(CargarParametros.getArchivoUsuarios(),
				mapaVehiculos);

		vehiculos.setAll(mapaVehiculos.values());
		usuarios.setAll(mapaUsuarios.values());
		servicios.setAll(Datos.cargarServicios(CargarParametros.getArchivoServicios()));
	}

	public double calcularCosto(Servicio servicio, double distanciaKm, double duracionMinutos) {

		return servicio.calcularCosto(distanciaKm, duracionMinutos);
	}
}
