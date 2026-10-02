/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define el vehículo que puede asignarse a un conductor y a un viaje.
 */
package vehiculo;

import java.util.Set;

import servicio.*;
import ubicacion.*;
import usuario.*;
import viajes.*;

public class Vehiculo {
	
	private String patente;
	private String modelo;
	
	private int capacidadPasajeros;
	
	private CategoriaVehiculo categoriaVehiculo;
	private TipoVehiculo tipoVehiculo;
	private Set<TipoServicio> tipoServicio; // Un vehiculo puede ser de ambos tipos al mismo tiempo.
	
	/**
	 * Crea un vehículo con su identificación, capacidad y características operativas.
	 *
	 * @param patente patente identificatoria del vehículo
	 * @param modelo modelo comercial del vehículo
	 * @param categoria categoría de servicio del vehículo
	 * @param tipo tipo físico del vehículo
	 * @param servicio tipos de servicio que puede prestar
	 */
	Vehiculo(String patente, String modelo, CategoriaVehiculo categoria,
			TipoVehiculo tipo, Set<TipoServicio> servicio) {
		
		this.patente = patente;
		this.modelo = modelo;
		categoriaVehiculo = categoria;
		tipoVehiculo = tipo;
		tipoServicio = servicio;
		
	}
	
	/**
	 * Agrega un tipo de servicio habilitado para el vehículo.
	 *
	 * @param tipoServicio tipo de servicio que se desea habilitar
	 */
	public void agregarTipoServicio(TipoServicio tipoServicio) {
		
	}

	// TODO: Incorporar getters, setters, hashCode y equals.
}
