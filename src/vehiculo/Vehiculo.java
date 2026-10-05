/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define el vehículo que puede asignarse a un conductor y a un viaje.
 */
package vehiculo;

import java.util.HashSet;
import java.util.Objects;
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
	public Vehiculo(String patente, String modelo, int capacidad, CategoriaVehiculo categoria,
			TipoVehiculo tipo, Set<TipoServicio> servicio) {
		
		this.patente = patente;
		this.modelo = modelo;
		this.capacidadPasajeros = capacidad;
		categoriaVehiculo = categoria;
		tipoVehiculo = tipo;
		tipoServicio = servicio;
		
	}


	public String getPatente() {
		return patente;
	}

	public void setPatente(String patente) {
		this.patente = patente;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}

	public void setCapacidadPasajeros(int capacidadPasajeros) {
		this.capacidadPasajeros = capacidadPasajeros;
	}

	public CategoriaVehiculo getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

	public void setCategoriaVehiculo(CategoriaVehiculo categoriaVehiculo) {
		this.categoriaVehiculo = categoriaVehiculo;
	}

	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
		this.tipoVehiculo = tipoVehiculo;
	}

	public Set<TipoServicio> getTipoServicio() {
		return new HashSet<>(this.tipoServicio);
	}


	/**
	 * Agrega un tipo de servicio habilitado para el vehículo.
	 *
	 * @param tipoServicio tipo de servicio que se desea habilitar
	 */
	public void agregarTipoServicio(TipoServicio nuevoServicio) {
		this.tipoServicio.add(nuevoServicio);
	}


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Vehiculo vehiculo = (Vehiculo) o;
		return Objects.equals(patente, vehiculo.patente);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(patente);
	}


	@Override
	public String toString() {
		return "Vehiculo{" +
				"patente='" + patente + '\'' +
				", modelo='" + modelo + '\'' +
				", capacidadPasajeros=" + capacidadPasajeros +
				", categoriaVehiculo=" + categoriaVehiculo +
				", tipoVehiculo=" + tipoVehiculo +
				", tipoServicio=" + tipoServicio +
				'}';
	}
}
