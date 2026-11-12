package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import aplicacion.ContextoAplicacion;
import servicio.Servicio;
import servicio.TipoServicio;
import simulacion.GeneradorUbicacionesViales;
import ubicacion.Ubicacion;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.Viaje;

/** Contexto + escenario inicial + hilos de los conductores simulados (GestorViajes). */
class ContextoAplicacionTest {

	private MotorFalso motor;
	private ContextoAplicacion contexto;
	private Usuario cliente;
	private List<Usuario> conductores;
	private Servicio estandar;

	private Usuario conductor(int n) {

		Usuario u = new Usuario("Conductor " + n, "1", "c" + n + "@x.com");
		u.altaConductor("LIC-" + n, new Vehiculo("AAA10" + n, "Etios", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)));
		return u;
	}

	@BeforeEach
	void preparar() {

		motor = new MotorFalso();
		contexto = new ContextoAplicacion(motor, new GeneradorUbicacionesViales(motor, new Random(7)));

		cliente = new Usuario("Cliente", "1", "cliente@x.com");
		conductores = new ArrayList<>(List.of(conductor(1), conductor(2), conductor(3), conductor(4), conductor(5)));
		estandar = new Servicio("Estandar", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);

		List<Usuario> todos = new ArrayList<>(conductores);
		todos.add(cliente);
		List<Vehiculo> vehiculos = conductores.stream().flatMap(u -> u.getConductor().getVehiculos().values().stream())
				.toList();

		contexto.cargar(todos, vehiculos, List.of(estandar));
	}

	@AfterEach
	void apagar() {

		contexto.close();
	}

	@Test
	void elEscenarioInicialPoneEnServicioYUbicaSobreCalleATodosLosConductores() {

		for (Usuario u : conductores) {

			assertEquals(RolUsuario.CONDUCTOR, u.getRolActivo());
			assertEquals(EstadoConductor.DISPONIBLE, u.getConductor().getEstadoConductor());
			assertNotNull(u.getConductor().getVehiculos().values().iterator().next().getUbicacion());
		}

		assertEquals(RolUsuario.CLIENTE, cliente.getRolActivo());
		assertEquals(6, contexto.getUsuarios().size());
		assertEquals(5, contexto.getUsuariosConRol(RolUsuario.CONDUCTOR).size());
	}

	@Test
	void laSimulacionUsaHastaTresConductoresYNuncaAlUsuarioDeLaSesion() {

		Usuario logueado = contexto.getUsuarios().stream().filter(u -> u.getEmail().equals("c1@x.com")).findFirst()
				.orElseThrow();
		contexto.setUsuarioLogueado(logueado);

		contexto.iniciarSimulacion();

		assertEquals(3, contexto.getConductoresSimulados().size());
		assertFalse(contexto.getConductoresSimulados().contains(logueado));
	}

	@Test
	void unViajeSolicitadoLoTomaUnHiloYElObservadorRecibeLosAvisos() throws InterruptedException {

		contexto.setUsuarioLogueado(cliente);
		BlockingQueue<EstadoViaje> avisos = new LinkedBlockingQueue<>();
		contexto.setObservadorViajes(v -> avisos.add(v.estadoActual()));
		contexto.iniciarSimulacion();

		Viaje viaje = new Viaje(cliente, new Ubicacion(-42.77, -65.04), new Ubicacion(-42.76, -65.03), estandar);
		viaje.solicitar(LocalDateTime.now());
		assertTrue(contexto.solicitarViaje(viaje));

		assertEquals(EstadoViaje.ACEPTADO, avisos.poll(5, TimeUnit.SECONDS));
		assertNotNull(viaje.getConductor());
		assertNotNull(viaje.getVehiculo());
		assertTrue(contexto.getConductoresSimulados().contains(viaje.getConductor()));

		// cerrar la sesion detiene los hilos y cancela el viaje en curso
		contexto.cerrarSesion();
		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
		assertTrue(contexto.getConductoresSimulados().isEmpty());
		assertNull(contexto.getUsuarioLogueado());
	}

	@Test
	void unConductorQueEntraComoClienteVuelveAEstarEnServicioAlSalir() {

		Usuario c = conductores.get(0);

		c.cambiarRolActivo(RolUsuario.CLIENTE);
		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, c.getConductor().getEstadoConductor());
		contexto.setUsuarioLogueado(c);

		contexto.cerrarSesion();

		assertEquals(RolUsuario.CONDUCTOR, c.getRolActivo());
		assertEquals(EstadoConductor.DISPONIBLE, c.getConductor().getEstadoConductor());
	}

	@Test
	void cerrarElContextoCierraElMotorVial() {

		contexto.close();

		assertTrue(motor.cerrado);
	}
}
