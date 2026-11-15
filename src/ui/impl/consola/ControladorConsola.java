/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de la interfaz de consola: lleva el dialogo (ingreso por mail, eleccion de rol, menu de usuario y de
 * conductor, tablas de datos) y delega todas las reglas de negocio en ContextoAplicacion, igual que los
 * controladores JavaFX. No lee ni escribe en la consola directamente: usa VistaConsola, por eso se prueba con
 * entradas escritas de antemano. Los puntos de origen y destino salen al azar sobre calles reales y se muestran
 * como nombre y altura de calle.
 */
package ui.impl.consola;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import aplicacion.ContextoAplicacion;
import aplicacion.ContextoAplicacion.SolicitudViaje;
import idioma.Mensajes;
import logica.Cotizador;
import modelo.Cotizacion;
import rutas.api.RutaNoDisponibleException;
import rutas.api.RutasViaje;
import servicio.Servicio;
import ubicacion.Ubicacion;
import usuario.Cliente;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.Viaje;

public class ControladorConsola {

	private static final long ESPERA_MS = 400;

	private final ContextoAplicacion contexto;
	private final VistaConsola vista;

	public ControladorConsola(ContextoAplicacion contexto, VistaConsola vista) {

		this.contexto = contexto;
		this.vista = vista;
	}

	/** Corre el dialogo hasta que el usuario sale o se cierra la entrada. */
	public void ejecutar() {

		contexto.setObservadorViajes(this::avisarCambio);

		try {

			vista.mostrar(Mensajes.get("consola.titulo"));
			vista.mostrar(estadoDatos());

			while (true) {

				String texto = vista.leer(Mensajes.get("consola.pedirMail"));

				if (texto == null || texto.equals("0")) {

					break;
				}

				if (texto.equalsIgnoreCase("i")) {

					cambiarIdioma();
					continue;
				}

				Optional<Usuario> usuario = contexto.buscarUsuarioPorEmail(texto);

				if (usuario.isEmpty()) {

					vista.mostrar(Mensajes.get("login.mailDesconocido"));
					continue;
				}

				if (!elegirRol(usuario.get())) {

					break; // se cerro la entrada
				}
			}

		} finally {

			contexto.setObservadorViajes(null);
		}
	}

	// ------------------------------------------------------------------ ingreso

	/** @return false si se cerro la entrada */
	private boolean elegirRol(Usuario usuario) {

		while (true) {

			vista.mostrar(Mensajes.get("consola.saludo", usuario.getNombre()));
			vista.mostrar(Mensajes.get("consola.menuRol"));
			String opcion = vista.leer(Mensajes.get("consola.opcion"));

			if (opcion == null) {

				return false;
			}

			switch (opcion) {

			case "1":
				if (ingresar(usuario, RolUsuario.CLIENTE) && !menuCliente(usuario)) {

					return false;
				}
				break;

			case "2":
				if (usuario.getConductor() == null) {

					vista.mostrar(Mensajes.get("login.noEsConductor"));

				} else if (ingresar(usuario, RolUsuario.CONDUCTOR) && !menuConductor(usuario)) {

					return false;
				}
				break;

			case "0":
				contexto.cerrarSesion();
				return true;

			default:
				vista.mostrar(Mensajes.get("consola.opcionInvalida"));
			}
		}
	}

	private boolean ingresar(Usuario usuario, RolUsuario rol) {

		try {

			if (usuario.getRolActivo() != rol) {

				usuario.cambiarRolActivo(rol);
			}

			contexto.setUsuarioLogueado(usuario);

			if (rol == RolUsuario.CLIENTE) {

				contexto.iniciarSimulacion(); // los conductores simulados toman los viajes del usuario
			}

			return true;

		} catch (IllegalStateException | IllegalArgumentException ex) {

			vista.mostrar(Mensajes.get("login.noSePudoIngresar", ex.getMessage()));
			return false;
		}
	}

	private void cambiarIdioma() {

		List<String> idiomas = Mensajes.IDIOMAS;
		int actual = idiomas.indexOf(Mensajes.getIdioma().getLanguage());
		Mensajes.setIdioma(idiomas.get((actual + 1) % idiomas.size()));
		vista.mostrar(Mensajes.get("consola.idiomaCambiado", Mensajes.nombreDeIdioma(Mensajes.getIdioma().getLanguage())));
	}

	// ------------------------------------------------------------------ usuario

	/** @return false si se cerro la entrada */
	private boolean menuCliente(Usuario usuario) {

		while (true) {

			vista.mostrar(Mensajes.get("consola.menuCliente"));
			String opcion = vista.leer(Mensajes.get("consola.opcion"));

			if (opcion == null) {

				return false;
			}

			switch (opcion) {

			case "1":
				solicitarViaje(usuario);
				break;

			case "2":
				mostrarEstado(ultimoViaje(usuario));
				break;

			case "3":
				cancelar(usuario);
				break;

			case "4":
				seguirViaje(ultimoViaje(usuario));
				break;

			case "0":
				contexto.cerrarSesion();
				return true;

			default:
				vista.mostrar(Mensajes.get("consola.opcionInvalida"));
			}
		}
	}

	private void solicitarViaje(Usuario usuario) {

		if (usuario.getCliente().enViaje()) {

			vista.mostrar(Mensajes.get("cliente.yaTieneViaje"));
			return;
		}

		Servicio servicio = elegirServicio();

		if (servicio == null) {

			return;
		}

		Ubicacion origen = contexto.generarUbicacionAleatoria();
		Ubicacion destino = contexto.generarUbicacionAleatoria();

		while (true) {

			Cotizacion cotizacion;

			try {

				cotizacion = Cotizador.cotizar(servicio, origen, destino);

			} catch (IllegalArgumentException ex) {

				vista.mostrar(ex.getMessage());
				destino = contexto.generarUbicacionAleatoria();
				continue;
			}

			vista.mostrar(Mensajes.get("cliente.origen", contexto.getGeocodificador().direccion(origen)));
			vista.mostrar(Mensajes.get("cliente.destino", contexto.getGeocodificador().direccion(destino)));
			vista.mostrar(Mensajes.get("cliente.cotizacion", cotizacion.distanciaKm(), cotizacion.duracionMinutos(),
					cotizacion.costo()));
			vista.mostrar(Mensajes.get("consola.menuPedido"));

			String opcion = vista.leer(Mensajes.get("consola.opcion"));

			if (opcion == null || opcion.equals("0")) {

				return;
			}

			if (opcion.equals("2")) {

				origen = contexto.generarUbicacionAleatoria();
				destino = contexto.generarUbicacionAleatoria();
				continue;
			}

			if (opcion.equals("1")) {

				pedir(usuario, servicio, origen, destino);
				return;
			}

			vista.mostrar(Mensajes.get("consola.opcionInvalida"));
		}
	}

	private Servicio elegirServicio() {

		List<Servicio> servicios = contexto.getServicios();

		if (servicios.isEmpty()) {

			vista.mostrar(Mensajes.get("cliente.sinServicios"));
			return null;
		}

		for (int i = 0; i < servicios.size(); i++) {

			vista.mostrar(String.format("  %d) %s", i + 1, servicios.get(i).getNombre()));
		}

		String texto = vista.leer(Mensajes.get("cliente.elegirServicio"));

		try {

			int indice = Integer.parseInt(texto == null ? "" : texto) - 1;
			return servicios.get(indice);

		} catch (NumberFormatException | IndexOutOfBoundsException ex) {

			vista.mostrar(Mensajes.get("consola.opcionInvalida"));
			return null;
		}
	}

	private void pedir(Usuario usuario, Servicio servicio, Ubicacion origen, Ubicacion destino) {

		SolicitudViaje solicitud;

		try {

			solicitud = contexto.pedirViaje(usuario, servicio, origen, destino);

		} catch (RutaNoDisponibleException | IllegalStateException ex) {

			vista.mostrar(Mensajes.get("cliente.noSePudoSolicitar", ex.getMessage()));
			return;
		}

		switch (solicitud.resultado()) {

		case SIN_CONDUCTORES:
			vista.mostrar(Mensajes.get("cliente.reservaRechazada"));
			break;

		case COLA_LLENA:
			vista.mostrar(Mensajes.get("cliente.colaLlena"));
			break;

		case ACEPTADA:
			RutasViaje rutas = solicitud.rutas();
			vista.mostrar(Mensajes.get("cliente.viajeSolicitado", solicitud.vehiculo().getPatente()));
			vista.mostrar(Mensajes.get("cliente.tramoConductor", rutas.haciaOrigen().distanciaKm(),
					rutas.haciaOrigen().duracionMinutos()));
			vista.mostrar(Mensajes.get("cliente.tramoViaje", rutas.haciaDestino().distanciaKm(),
					rutas.haciaDestino().duracionMinutos()));
			seguirViaje(solicitud.viaje());
			break;
		}
	}

	private void cancelar(Usuario usuario) {

		Viaje viaje = ultimoViaje(usuario);

		if (viaje == null || !usuario.getCliente().enViaje()) {

			vista.mostrar(Mensajes.get("cliente.sinViajeActivo"));
			return;
		}

		String motivo = vista.leer(Mensajes.get("cliente.motivoCancelacion"));

		try {

			contexto.cancelarViaje(viaje, usuario, motivo == null || motivo.isBlank()
					? Mensajes.get("cliente.motivoPorDefecto") : motivo);

		} catch (IllegalStateException | IllegalArgumentException ex) {

			vista.mostrar(Mensajes.get("cliente.noSePudoCancelar", ex.getMessage()));
		}
	}

	/** Muestra los cambios del viaje hasta que termina; con Enter se vuelve al menu sin cancelarlo. */
	private void seguirViaje(Viaje viaje) {

		if (viaje == null) {

			vista.mostrar(Mensajes.get("cliente.sinViaje"));
			return;
		}

		vista.mostrar(Mensajes.get("consola.siguiendo"));

		while (!terminado(viaje.estadoActual())) {

			if (vista.hayEntrada()) {

				vista.leer("");
				return;
			}

			try {

				Thread.sleep(ESPERA_MS);

			} catch (InterruptedException e) {

				Thread.currentThread().interrupt();
				return;
			}
		}

		mostrarEstado(viaje);
	}

	private static boolean terminado(EstadoViaje estado) {

		return estado == EstadoViaje.FINALIZADO || estado == EstadoViaje.CANCELADO
				|| estado == EstadoViaje.RECHAZADO;
	}

	private void mostrarEstado(Viaje viaje) {

		if (viaje == null) {

			vista.mostrar(Mensajes.get("cliente.sinViaje"));
			return;
		}

		vista.mostrar(Mensajes.get("cliente.estadoViaje", Mensajes.get("estado." + viaje.estadoActual().name())));
	}

	private Viaje ultimoViaje(Usuario usuario) {

		List<Viaje> viajes = usuario.getCliente() == null ? List.of() : usuario.getCliente().getViajes();
		return viajes.isEmpty() ? null : viajes.getLast();
	}

	/** Lo llaman los hilos de los conductores: solo se avisa del viaje del usuario logueado. */
	private void avisarCambio(Viaje viaje) {

		Usuario logueado = contexto.getUsuarioLogueado();

		if (logueado != null && viaje.getCliente() == logueado) {

			vista.mostrar(Mensajes.get("consola.avisoViaje", Mensajes.get("estado." + viaje.estadoActual().name())));
		}
	}

	// ------------------------------------------------------------------ conductor

	/** @return false si se cerro la entrada */
	private boolean menuConductor(Usuario usuario) {

		while (true) {

			vista.mostrar(Mensajes.get("consola.menuConductor"));
			String opcion = vista.leer(Mensajes.get("consola.opcion"));

			if (opcion == null) {

				return false;
			}

			switch (opcion) {

			case "1":
				tablaViajes(usuario.getConductor().getViajes());
				break;

			case "2":
				if (!menuTablas()) {

					return false;
				}
				break;

			case "0":
				contexto.cerrarSesion();
				return true;

			default:
				vista.mostrar(Mensajes.get("consola.opcionInvalida"));
			}
		}
	}

	private boolean menuTablas() {

		while (true) {

			vista.mostrar(Mensajes.get("consola.menuTablas"));
			String opcion = vista.leer(Mensajes.get("consola.opcion"));

			if (opcion == null) {

				return false;
			}

			switch (opcion) {

			case "1":
				tablaVehiculos();
				break;

			case "2":
				tablaUsuarios();
				break;

			case "3":
				tablaServicios();
				break;

			case "4":
				tablaViajes(todosLosViajes());
				break;

			case "0":
				return true;

			default:
				vista.mostrar(Mensajes.get("consola.opcionInvalida"));
			}
		}
	}

	private List<Viaje> todosLosViajes() {

		List<Viaje> viajes = new ArrayList<>();

		for (Usuario u : contexto.getUsuarios()) {

			Cliente cliente = u.getCliente();

			if (cliente != null) {

				viajes.addAll(cliente.getViajes());
			}
		}

		return viajes;
	}

	private void tablaVehiculos() {

		List<List<String>> filas = new ArrayList<>();

		for (Vehiculo v : contexto.getVehiculos()) {

			filas.add(List.of(v.getPatente(), v.getModelo(), String.valueOf(v.getCapacidadPasajeros()),
					String.valueOf(v.getTipoVehiculo()), String.valueOf(v.getCategoriaVehiculo()),
					v.getUbicacion() == null ? "-" : contexto.getGeocodificador().direccion(v.getUbicacion())));
		}

		vista.mostrarTabla(List.of(Mensajes.get("tabla.patente"), Mensajes.get("tabla.modelo"),
				Mensajes.get("tabla.capacidad"), Mensajes.get("tabla.tipo"), Mensajes.get("tabla.categoria"),
				Mensajes.get("tabla.ubicacion")), filas);
	}

	private void tablaUsuarios() {

		List<List<String>> filas = new ArrayList<>();

		for (Usuario u : contexto.getUsuarios()) {

			filas.add(List.of(u.getNombre(), u.getEmail(), u.getTelefono(),
					Mensajes.get(u.getConductor() != null ? "tabla.si" : "tabla.no")));
		}

		vista.mostrarTabla(List.of(Mensajes.get("tabla.nombre"), Mensajes.get("tabla.email"),
				Mensajes.get("tabla.telefono"), Mensajes.get("tabla.esConductor")), filas);
	}

	private void tablaServicios() {

		List<List<String>> filas = new ArrayList<>();

		for (Servicio s : contexto.getServicios()) {

			filas.add(List.of(s.getNombre(), String.valueOf(s.getTipoServicio()), String.valueOf(s.getTipoVehiculo()),
					String.valueOf(s.getCategoriaVehiculo()), String.format(Locale.ROOT, "%.2f", s.getTarifaBase()),
					String.format(Locale.ROOT, "%.2f", s.getPrecioKm()),
					String.format(Locale.ROOT, "%.2f", s.getPrecioMinuto())));
		}

		vista.mostrarTabla(List.of(Mensajes.get("tabla.nombre"), Mensajes.get("tabla.tipoServicio"),
				Mensajes.get("tabla.tipo"), Mensajes.get("tabla.categoria"), Mensajes.get("tabla.tarifaBase"),
				Mensajes.get("tabla.precioKm"), Mensajes.get("tabla.precioMinuto")), filas);
	}

	private void tablaViajes(List<Viaje> viajes) {

		List<List<String>> filas = new ArrayList<>();

		for (Viaje v : viajes) {

			filas.add(List.of(v.getId().toString().substring(0, 8), v.getCliente().getNombre(),
					v.getConductor() == null ? "-" : v.getConductor().getNombre(), v.getServicio().getNombre(),
					Mensajes.get("estado." + v.estadoActual().name())));
		}

		vista.mostrarTabla(List.of(Mensajes.get("tabla.id"), Mensajes.get("tabla.cliente"),
				Mensajes.get("tabla.conductor"), Mensajes.get("tabla.servicio"), Mensajes.get("tabla.estado")), filas);
	}

	private String estadoDatos() {

		return Mensajes.get("login.estadoDatos", contexto.getOrigen(), contexto.getUsuarios().size(),
				contexto.getVehiculos().size(), contexto.getServicios().size());
	}
}
