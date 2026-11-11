package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.GestorViajes;
import servicio.Servicio;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.Viaje;

/**
 * Pruebas del GestorViajes: asignación de viajes a conductores simulados, simulación del ciclo de
 * vida, rechazo cuando nadie puede tomar el viaje y apagado limpio.
 *
 * Los viajes simulados duran unos 8 segundos (4 por tramo), por eso las esperas usan un tiempo
 * máximo generoso en lugar de dormir una cantidad fija.
 */
class GestorViajesTest {

	private GestorViajes gestor;
	private final List<EstadoViaje> avisos = new CopyOnWriteArrayList<>();

	private final Ubicacion origen = new Ubicacion(-42.770, -65.040);
	private final Ubicacion destino = new Ubicacion(-42.760, -65.030);

	private Servicio servicioEstandar;
	private Servicio servicioPremium;

	@BeforeEach
	void preparar() {

		avisos.clear();

		servicioEstandar = new Servicio("Estandar", 2000, 200, 100, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
		servicioPremium = new Servicio("Premium", 3000, 300, 150, TipoVehiculo.AUTO,
				CategoriaVehiculo.PREMIUM, TipoServicio.PASAJEROS);

		gestor = new GestorViajes(viaje -> avisos.add(viaje.estadoActual()));
	}

	@AfterEach
	void apagar() {

		// Nunca debe quedar un hilo de simulación vivo entre pruebas.
		gestor.detener();
	}

	@Test
	@DisplayName("1. Un conductor compatible toma el viaje y lo completa solo")
	void completaElCicloDelViajeConUnConductor() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Uno");
		Usuario conductor = crearConductor("Ana", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor));

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje), "El gestor debería aceptar la solicitud.");

		assertTrue(esperarEstado(viaje, EstadoViaje.FINALIZADO, 20_000),
				"El viaje no llegó a FINALIZADO.");

		assertEquals(conductor, viaje.getConductor(), "El viaje debe quedar asignado al conductor.");
		assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor(),
				"El conductor debe quedar DISPONIBLE al terminar.");
		assertFalse(cliente.getCliente().enViaje(), "El cliente debe quedar libre para otro viaje.");
		assertTrue(avisos.containsAll(List.of(EstadoViaje.ACEPTADO, EstadoViaje.INICIADO,
				EstadoViaje.FINALIZADO)), "Debería avisar cada cambio de estado.");
	}

	@Test
	@DisplayName("2. Un conductor con vehículo de categoría menor no se queda con el viaje")
	void elViajeLoTerminaElConductorCompatible() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Dos");
		Usuario basico = crearConductor("Beto", CategoriaVehiculo.ESTANDAR);
		Usuario premium = crearConductor("Pia", CategoriaVehiculo.PREMIUM);

		gestor.iniciar(List.of(basico, premium));

		Viaje viaje = crearViajeSolicitado(cliente, servicioPremium);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.ACEPTADO, 10_000),
				"El viaje debería ser aceptado por el conductor con vehículo PREMIUM.");
		assertEquals(premium, viaje.getConductor(),
				"El conductor con vehículo ESTANDAR no puede cubrir un servicio PREMIUM.");
	}

	@Test
	@DisplayName("3. Si ningún conductor es compatible el viaje queda RECHAZADO")
	void rechazaElViajeCuandoNadiePuedeTomarlo() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Tres");
		Usuario basico = crearConductor("Bruno", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(basico));

		Viaje viaje = crearViajeSolicitado(cliente, servicioPremium);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.RECHAZADO, 15_000),
				"El viaje no debería quedar rebotando para siempre.");
		assertNull(viaje.getConductor(), "Ningún conductor debe quedar asignado.");
		assertEquals(EstadoConductor.DISPONIBLE, basico.getConductor().getEstadoConductor(),
				"El conductor incompatible debe seguir disponible.");
	}

	@Test
	@DisplayName("4. Detener cancela el viaje en curso y libera al conductor")
	void detenerCancelaElViajeEnCurso() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Cuatro");
		Usuario conductor = crearConductor("Carla", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor));

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));
		assertTrue(esperarEstado(viaje, EstadoViaje.ACEPTADO, 10_000),
				"El conductor debería aceptar el viaje.");

		gestor.detener();

		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual(),
				"El viaje en curso debe quedar cancelado al detener la simulación.");
		assertEquals("Simulacion detenida", viaje.getMotivoCancelacion());
		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, conductor.getConductor().getEstadoConductor());
		assertFalse(cliente.getCliente().enViaje(), "El cliente debe quedar libre.");
	}

	@Test
	@DisplayName("5. Detener cancela los viajes que quedaron esperando en la cola")
	void detenerCancelaLosViajesPendientes() {

		Usuario cliente = crearCliente("Cliente Cinco");
		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		gestor.detener();

		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual(),
				"Un viaje pendiente no debe sobrevivir al apagado.");
		assertEquals("Simulacion detenida", viaje.getMotivoCancelacion());
	}

	@Test
	@DisplayName("6. Un usuario sin perfil de conductor no atiende viajes")
	void noUsaUsuariosSinPerfilDeConductor() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Seis");
		Usuario pasajero = crearCliente("Pasajero");

		gestor.iniciar(List.of(pasajero));

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		Thread.sleep(500);

		assertEquals(EstadoViaje.SOLICITADO, viaje.estadoActual(),
				"Nadie debería haber tomado el viaje.");
		assertNull(viaje.getConductor());
	}

	@Test
	@DisplayName("7. La cola rechaza la solicitud cuando está llena")
	void noAceptaMasDeCincuentaViajesEnCola() {

		for (int i = 1; i <= 50; i++) {

			Viaje viaje = crearViajeSolicitado(crearCliente("Cliente Cola " + i), servicioEstandar);
			assertTrue(gestor.solicitar(viaje), "Debería aceptar el viaje número " + i + ".");
		}

		Viaje sobrante = crearViajeSolicitado(crearCliente("Cliente Cola 51"), servicioEstandar);

		assertFalse(gestor.solicitar(sobrante), "La solicitud 51 no entra en la cola.");
	}

	@Test
	@DisplayName("8. Un error al avisar a la interfaz no corta la simulación")
	void unErrorAlAvisarNoCortaLaSimulacion() throws InterruptedException {

		gestor = new GestorViajes(viaje -> {

			throw new IllegalStateException("La pantalla falló");
		});

		Usuario cliente = crearCliente("Cliente Ocho");
		Usuario conductor = crearConductor("Diego", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor));

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.INICIADO, 10_000),
				"El hilo del conductor no debería morir porque falle el aviso a la pantalla.");
	}

	@Test
	@DisplayName("9. Un viaje cancelado antes de ser tomado no se asigna")
	void noAsignaUnViajeQueYaFueCancelado() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Nueve");
		Usuario conductor = crearConductor("Elsa", CategoriaVehiculo.ESTANDAR);

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		viaje.cancelar(LocalDateTime.now(), cliente, "El plan cambió");
		assertTrue(gestor.solicitar(viaje));

		gestor.iniciar(List.of(conductor));

		Thread.sleep(500);

		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual(),
				"El conductor no debe revivir un viaje cancelado.");
		assertNull(viaje.getConductor());
		assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
	}

	// ---------------------------------------------------------------------------------------------
	// Ayudas
	// ---------------------------------------------------------------------------------------------

	private Usuario crearCliente(String nombre) {

		return new Usuario(nombre, "tel-" + nombre, nombre.replace(' ', '.') + "@x.com");
	}

	private Usuario crearConductor(String nombre, CategoriaVehiculo categoria) {

		Usuario usuario = new Usuario(nombre, "tel-" + nombre, nombre + "@x.com");
		usuario.altaConductor("LIC-" + nombre,
				new Vehiculo("PAT-" + nombre, "Auto", 4, TipoVehiculo.AUTO, categoria,
						TipoServicio.PASAJEROS));
		usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);

		return usuario;
	}

	private Viaje crearViajeSolicitado(Usuario cliente, Servicio servicio) {

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());

		return viaje;
	}

	/**
	 * Espera hasta que el viaje llegue al estado pedido o hasta que se acabe el tiempo máximo.
	 *
	 * @return true si el viaje llegó al estado esperado
	 */
	private boolean esperarEstado(Viaje viaje, EstadoViaje esperado, long tiempoMaximoMs)
			throws InterruptedException {

		long limite = System.currentTimeMillis() + tiempoMaximoMs;

		while (System.currentTimeMillis() < limite) {

			if (viaje.estadoActual() == esperado) {

				return true;
			}
			Thread.sleep(25);
		}

		return viaje.estadoActual() == esperado;
	}
}
