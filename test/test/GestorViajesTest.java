package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.ConfiguracionSimulacion;
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
 * Pruebas del GestorViajes: asignación a todos los conductores compatibles, simulación del ciclo
 * de vida, expiración por tiempo y apagado limpio.
 *
 * Los tiempos de la simulación se pasan en la configuración, así las pruebas no esperan los 8
 * segundos de la aplicación real.
 */
class GestorViajesTest {

	private static final ConfiguracionSimulacion RAPIDA = new ConfiguracionSimulacion(0.2, 10, 2.0, 50);
	private static final ConfiguracionSimulacion LENTA = new ConfiguracionSimulacion(30.0, 10, 60.0, 50);
	private static final ConfiguracionSimulacion EXPIRA_RAPIDO = new ConfiguracionSimulacion(0.2, 10, 0.5, 50);

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

		gestor.iniciar(List.of(conductor), RAPIDA);

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje), "El gestor debería aceptar la solicitud.");

		assertTrue(esperarEstado(viaje, EstadoViaje.FINALIZADO, 5_000),
				"El viaje no llegó a FINALIZADO.");

		assertEquals(conductor, viaje.getConductor(), "El viaje debe quedar asignado al conductor.");
		assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor(),
				"El conductor debe quedar DISPONIBLE al terminar.");
		assertFalse(cliente.getCliente().enViaje(), "El cliente debe quedar libre para otro viaje.");
		assertTrue(avisos.containsAll(List.of(EstadoViaje.ACEPTADO, EstadoViaje.INICIADO,
				EstadoViaje.FINALIZADO)), "Debería avisar cada cambio de estado.");
	}

	@Test
	@DisplayName("2. El viaje se ofrece a todos los compatibles y lo toma uno solo")
	void ofreceElViajeATodosLosConductoresCompatibles() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Dos");
		Usuario ana = crearConductor("Ana", CategoriaVehiculo.ESTANDAR);
		Usuario beto = crearConductor("Beto", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(ana, beto), RAPIDA);

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.ACEPTADO, 5_000),
				"El viaje debería ser aceptado por uno de los dos conductores.");

		Usuario ganador = viaje.getConductor();
		Usuario perdedor = ganador.equals(ana) ? beto : ana;

		assertTrue(ganador.equals(ana) || ganador.equals(beto), "El viaje debe quedar en un conductor.");
		assertTrue(ganador.getConductor().getViajes().contains(viaje),
				"El ganador debe tener el viaje en su registro.");
		assertTrue(perdedor.getConductor().getViajes().isEmpty(),
				"El otro conductor no debe quedarse con una copia del viaje.");
	}

	@Test
	@DisplayName("3. Un vehículo de categoría menor no recibe un viaje PREMIUM")
	void elViajeLoTerminaElConductorDeCategoriaCompatible() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Tres");
		Usuario basico = crearConductor("Bruno", CategoriaVehiculo.ESTANDAR);
		Usuario premium = crearConductor("Pia", CategoriaVehiculo.PREMIUM);

		gestor.iniciar(List.of(basico, premium), RAPIDA);

		Viaje viaje = crearViajeSolicitado(cliente, servicioPremium);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.ACEPTADO, 5_000),
				"El viaje debería ser aceptado por el conductor PREMIUM.");
		assertEquals(premium, viaje.getConductor(),
				"El conductor con vehículo ESTANDAR no puede cubrir un servicio PREMIUM.");
	}

	@Test
	@DisplayName("4. Un vehículo de otro tipo de servicio no recibe el viaje")
	void noOfreceElViajeAUnConductorDeOtroTipoDeServicio() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Cuatro");
		Usuario envios = crearConductor("Enzo", CategoriaVehiculo.ESTANDAR, TipoServicio.ENVIOS);

		gestor.iniciar(List.of(envios), EXPIRA_RAPIDO);

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.RECHAZADO, 5_000),
				"El viaje no debería asignarse a un vehículo de ENVIOS.");
		assertNull(viaje.getConductor());
	}

	@Test
	@DisplayName("5. Si nadie lo acepta a tiempo, el viaje queda RECHAZADO")
	void rechazaElViajeSiNadieLoAceptaEnElTiempoMaximo() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Cinco");
		Usuario basico = crearConductor("Bruno", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(basico), EXPIRA_RAPIDO);

		Viaje viaje = crearViajeSolicitado(cliente, servicioPremium);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.RECHAZADO, 5_000),
				"El viaje debería rechazarse al cumplirse el tiempo máximo.");
		assertTrue(avisos.contains(EstadoViaje.RECHAZADO), "El cliente debe recibir el aviso de rechazo.");
		assertNull(viaje.getConductor());
		assertEquals(EstadoConductor.DISPONIBLE, basico.getConductor().getEstadoConductor());
	}

	@Test
	@DisplayName("6. Detener cancela el viaje en curso y los pendientes")
	void detenerCancelaElViajeEnCursoYLosPendientes() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Seis");
		Usuario conductor = crearConductor("Carla", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor), LENTA);

		Viaje enCurso = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(enCurso));
		assertTrue(esperarEstado(enCurso, EstadoViaje.ACEPTADO, 5_000));

		Usuario otroCliente = crearCliente("Cliente Seis Bis");
		Viaje pendiente = crearViajeSolicitado(otroCliente, servicioEstandar);
		assertTrue(gestor.solicitar(pendiente));

		gestor.detener();

		assertEquals(EstadoViaje.CANCELADO, enCurso.estadoActual());
		assertEquals(EstadoViaje.CANCELADO, pendiente.estadoActual());
		assertEquals("Simulacion detenida", enCurso.getMotivoCancelacion());
		assertEquals("Simulacion detenida", pendiente.getMotivoCancelacion());
		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, conductor.getConductor().getEstadoConductor());
	}

	@Test
	@DisplayName("7. La bandeja de un conductor rechaza solicitudes cuando está llena")
	void noAceptaMasViajesQueLaCapacidadDeLaBandeja() throws InterruptedException {

		ConfiguracionSimulacion chica = new ConfiguracionSimulacion(30.0, 10, 60.0, 2);

		Usuario cliente = crearCliente("Cliente Siete");
		Usuario conductor = crearConductor("Diego", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor), chica);

		Viaje enCurso = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(enCurso));
		assertTrue(esperarEstado(enCurso, EstadoViaje.ACEPTADO, 5_000));

		Viaje primero = crearViajeSolicitado(crearCliente("Cliente Siete A"), servicioEstandar);
		Viaje segundo = crearViajeSolicitado(crearCliente("Cliente Siete B"), servicioEstandar);
		Viaje sobrante = crearViajeSolicitado(crearCliente("Cliente Siete C"), servicioEstandar);

		assertTrue(gestor.solicitar(primero));
		assertTrue(gestor.solicitar(segundo));
		assertFalse(gestor.solicitar(sobrante), "La bandeja llena debe rechazar la solicitud.");
	}

	@Test
	@DisplayName("8. Un error al avisar a la interfaz no corta la simulación")
	void unErrorAlAvisarNoCortaLaSimulacion() throws InterruptedException {

		gestor = new GestorViajes(viaje -> {

			throw new IllegalStateException("La pantalla falló");
		});

		Usuario cliente = crearCliente("Cliente Ocho");
		Usuario conductor = crearConductor("Elsa", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor), RAPIDA);

		Viaje viaje = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(viaje));

		assertTrue(esperarEstado(viaje, EstadoViaje.INICIADO, 5_000),
				"El hilo del conductor no debería morir porque falle el aviso a la pantalla.");
	}

	@Test
	@DisplayName("9. Un viaje cancelado antes de ser tomado no se asigna")
	void noAsignaUnViajeQueYaFueCancelado() throws InterruptedException {

		Usuario cliente = crearCliente("Cliente Nueve");
		Usuario conductor = crearConductor("Fabi", CategoriaVehiculo.ESTANDAR);

		gestor.iniciar(List.of(conductor), LENTA);

		Viaje enCurso = crearViajeSolicitado(cliente, servicioEstandar);
		assertTrue(gestor.solicitar(enCurso));
		assertTrue(esperarEstado(enCurso, EstadoViaje.ACEPTADO, 5_000));

		Usuario otroCliente = crearCliente("Cliente Nueve Bis");
		Viaje cancelado = crearViajeSolicitado(otroCliente, servicioEstandar);
		assertTrue(gestor.solicitar(cancelado));
		cancelado.cancelar(LocalDateTime.now(), otroCliente, "El plan cambió");

		gestor.detener();

		assertEquals(EstadoViaje.CANCELADO, cancelado.estadoActual());
		assertEquals("El plan cambió", cancelado.getMotivoCancelacion(),
				"El apagado no debe pisar el motivo de una cancelación del cliente.");
		assertNull(cancelado.getConductor());
	}

	@Test
	@DisplayName("10. No se puede encolar un viaje nulo")
	void noAceptaUnViajeNulo() {

		assertThrows(IllegalArgumentException.class, () -> gestor.solicitar(null));
	}

	// ---------------------------------------------------------------------------------------------
	// Ayudas
	// ---------------------------------------------------------------------------------------------

	private Usuario crearCliente(String nombre) {

		return new Usuario(nombre, "tel-" + nombre, nombre.replace(' ', '.') + "@x.com");
	}

	private Usuario crearConductor(String nombre, CategoriaVehiculo categoria) {

		return crearConductor(nombre, categoria, TipoServicio.PASAJEROS);
	}

	private Usuario crearConductor(String nombre, CategoriaVehiculo categoria, TipoServicio tipoServicio) {

		Usuario usuario = new Usuario(nombre, "tel-" + nombre, nombre + "@x.com");
		usuario.altaConductor("LIC-" + nombre,
				new Vehiculo("PAT-" + nombre, "Auto", 4, TipoVehiculo.AUTO, categoria, tipoServicio));
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
