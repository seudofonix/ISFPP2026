package vehiculo;

import java.util.Set;

import servicio.*;
import ubicacion.*;
import usuario.*;
import viajes.*;

/**
 * Instancia Supervisada de Formacion Practica Profesional (ISFPP) Ignacio Bullon, 2026. UNPSJB.
 *
 * Representa el medio de transporte asignable a un conductor y a los viajes del servicio.
 */
public class Vehiculo {
	
	private String patente;
	private String modelo;
	
	private int capacidadPasajeros;
	
	private CategoriaVehiculo categoriaVehiculo;
	private TipoVehiculo tipoVehiculo;
	private Set<TipoServicio> tipoServicio; // Un vehiculo puede ser de ambos tipos al mismo tiempo.
	
	Vehiculo(String patente, String modelo, CategoriaVehiculo categoria,
			TipoVehiculo tipo, Set<TipoServicio> servicio) {
		
		this.patente = patente;
		this.modelo = modelo;
		categoriaVehiculo = categoria;
		tipoVehiculo = tipo;
		tipoServicio = servicio;
		
	}
	
	public void agregarTipoServicio(TipoServicio tipoServicio) {
		
	}
}
