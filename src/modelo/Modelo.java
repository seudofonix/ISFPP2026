/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * MODELO del patron MVC. Guarda los datos del sistema en colecciones observables de JavaFX, la sesion
 * del usuario que ingreso (como usuario o como conductor) y la logica de cotizar y reservar viajes. No
 * conoce ni las vistas ni los controladores.
 */
package modelo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import datos.CargarParametros;
import datos.Datos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.Viaje;

public class Modelo {

	private final ObservableList<Vehiculo> vehiculos = FXCollections.observableArrayList();
	private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
	private final ObservableList<Servicio> servicios = FXCollections.observableArrayList();
	private final ObservableList<Viaje> reservas = FXCollections.observableArrayList();

	private final Random azar = new Random();

	private Usuario usuarioActual;

	public ObservableList<Vehiculo> getVehiculos() {

		return vehiculos;
	}

	public ObservableList<Usuario> getUsuarios() {

		return usuarios;
	}

	public ObservableList<Servicio> getServicios() {

		return servicios;
	}

	/** Reservas (viajes) del usuario que tiene la sesion iniciada. */
	public ObservableList<Viaje> getReservas() {

		return reservas;
	}

	public Usuario getUsuarioActual() {

		return usuarioActual;
	}

	// ------------------------------------------------------------------ datos

	/**
	 * (Re)carga todos los datos segun config.properties (archivos o base de datos). Como se crean objetos
	 * nuevos, cierra la sesion actual.
	 */
	public void cargar() throws IOException {

		CargarParametros.parametros();

		HashMap<String, Vehiculo> mapaVehiculos = Datos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
		HashMap<String, Usuario> mapaUsuarios = Datos.cargarUsuarios(CargarParametros.getArchivoUsuarios(),
				mapaVehiculos);
		List<Servicio> listaServicios = Datos.cargarServicios(CargarParametros.getArchivoServicios());

		usuarioActual = null;
		reservas.clear();

		vehiculos.setAll(mapaVehiculos.values().stream().sorted(Comparator.comparing(Vehiculo::getPatente)).toList());
		usuarios.setAll(mapaUsuarios.values().stream()
				.sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
		servicios.setAll(listaServicios);
	}

	// ------------------------------------------------------------------ sesion

	/** Usuarios que pueden ingresar con el rol indicado (todos como cliente; solo conductores como conductor). */
	public List<Usuario> getUsuariosConRol(RolUsuario rol) {

		return usuarios.stream().filter(u -> rol != RolUsuario.CONDUCTOR || u.getConductor() != null)
				.sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre())).toList();
	}

	/**
	 * Inicia la sesion con el rol elegido, aplicando las reglas del dominio (por ejemplo, un conductor
	 * debe estar dado de alta y no puede tener un viaje activo como cliente).
	 *
	 * @throws IllegalStateException si el dominio no permite ese rol
	 */
	public void iniciarSesion(Usuario usuario, RolUsuario rol) {

		Objects.requireNonNull(usuario, "usuario");
		Objects.requireNonNull(rol, "rol");

		if (usuario.getRolActivo() != rol) {

			usuario.cambiarRolActivo(rol);
		}

		usuarioActual = usuario;
		reservas.setAll(usuario.getCliente().getViajes());
	}

	/** Cierra la sesion; un conductor vuelve a quedar fuera de servicio. */
	public void cerrarSesion() {

		if (usuarioActual != null && usuarioActual.getRolActivo() != RolUsuario.CLIENTE) {

			usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
		}

		usuarioActual = null;
		reservas.clear();
	}

	// ------------------------------------------------------------------ cotizar y reservar

	/**
	 * Estima el viaje: distancia geografica (Haversine) entre origen y destino, duracion con la velocidad
	 * media configurada y costo = tarifaBase + km * precioKm + minutos * precioMinuto.
	 *
	 * @throws IllegalArgumentException si falta un dato, un punto esta fuera del area o son iguales
	 */
	public Cotizacion cotizar(Servicio servicio, Ubicacion origen, Ubicacion destino) {

		if (servicio == null) {

			throw new IllegalArgumentException("Elegi un servicio.");
		}

		validarUbicacion(origen, "origen");
		validarUbicacion(destino, "destino");

		double km = origen.calcularDistancia(destino);

		if (km <= 0) {

			throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo punto.");
		}

		double minutos = km / CargarParametros.getVelocidadMediaKmh() * 60.0;

		return new Cotizacion(km, minutos, servicio.calcularCosto(km, minutos));
	}

	/**
	 * Reserva un viaje para el usuario con sesion de cliente: lo crea y lo solicita (queda SOLICITADO),
	 * listo para que luego se le asigne conductor y se calcule la ruta.
	 *
	 * @throws IllegalStateException    si no hay sesion de cliente o ya tiene un viaje activo
	 * @throws IllegalArgumentException si los datos no son validos
	 */
	public Viaje reservarViaje(Servicio servicio, Ubicacion origen, Ubicacion destino) {

		if (usuarioActual == null || usuarioActual.getRolActivo() != RolUsuario.CLIENTE) {

			throw new IllegalStateException("Para reservar tenes que ingresar como usuario.");
		}

		cotizar(servicio, origen, destino);

		Viaje viaje = new Viaje(usuarioActual, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());
		reservas.add(viaje);

		return viaje;
	}

	/**
	 * Cancela una reserva que todavia no fue aceptada por ningun conductor.
	 *
	 * @throws IllegalStateException si la reserva no esta en estado SOLICITADO
	 */
	public void cancelarReserva(Viaje viaje) {

		if (usuarioActual == null || viaje == null || viaje.getCliente() != usuarioActual) {

			throw new IllegalStateException("La reserva no pertenece al usuario actual.");
		}

		if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {

			throw new IllegalStateException("Solo se puede cancelar una reserva en estado SOLICITADO.");
		}

		viaje.cancelar(LocalDateTime.now(), usuarioActual, "Cancelada por el cliente");

		int posicion = reservas.indexOf(viaje);

		if (posicion >= 0) {

			reservas.set(posicion, viaje); // fuerza el refresco de la tabla
		}
	}

	// ------------------------------------------------------------------ ubicaciones

	/** Punto aleatorio dentro del area de simulacion (todavia sin ajustar a calle). */
	public Ubicacion generarUbicacionAleatoria() {

		double latitud = entre(CargarParametros.getLatitud1(), CargarParametros.getLatitud2());
		double longitud = entre(CargarParametros.getLongitud1(), CargarParametros.getLongitud2());

		return new Ubicacion(redondear(latitud), redondear(longitud));
	}

	private void validarUbicacion(Ubicacion ubicacion, String nombre) {

		if (ubicacion == null) {

			throw new IllegalArgumentException("Falta completar el " + nombre + ".");
		}

		double latMin = Math.min(CargarParametros.getLatitud1(), CargarParametros.getLatitud2());
		double latMax = Math.max(CargarParametros.getLatitud1(), CargarParametros.getLatitud2());
		double lonMin = Math.min(CargarParametros.getLongitud1(), CargarParametros.getLongitud2());
		double lonMax = Math.max(CargarParametros.getLongitud1(), CargarParametros.getLongitud2());

		boolean dentro = ubicacion.getLatitud() >= latMin && ubicacion.getLatitud() <= latMax
				&& ubicacion.getLongitud() >= lonMin && ubicacion.getLongitud() <= lonMax;

		if (!dentro) {

			throw new IllegalArgumentException(String.format(java.util.Locale.ROOT,
					"El %s esta fuera del area de simulacion (latitud %.3f a %.3f, longitud %.3f a %.3f).", nombre,
					latMin, latMax, lonMin, lonMax));
		}
	}

	private double entre(double a, double b) {

		double min = Math.min(a, b);
		double max = Math.max(a, b);

		return min + azar.nextDouble() * (max - min);
	}

	private double redondear(double valor) {

		return Math.round(valor * 1_000_000.0) / 1_000_000.0;
	}
}
