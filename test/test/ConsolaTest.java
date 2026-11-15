package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import aplicacion.ContextoAplicacion;
import idioma.Mensajes;
import servicio.Servicio;
import servicio.TipoServicio;
import simulacion.GeneradorUbicacionesViales;
import ui.impl.consola.ControladorConsola;
import ui.impl.consola.VistaConsola;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.Viaje;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Interfaz de consola con la entrada escrita de antemano: mismas reglas que la interfaz JavaFX. */
class ConsolaTest {

	private ContextoAplicacion contexto;
	private Usuario cliente;
	private Usuario conductorPuro;

	@BeforeEach
	void preparar() {

		Mensajes.setIdioma("es");
		MotorFalso motor = new MotorFalso();
		contexto = new ContextoAplicacion(motor, new GeneradorUbicacionesViales(motor, new Random(7)));

		cliente = new Usuario("Cliente", "1", "cliente@x.com");
		List<Usuario> todos = new ArrayList<>(List.of(cliente));
		List<Vehiculo> vehiculos = new ArrayList<>();

		for (int n = 1; n <= 2; n++) {

			Usuario u = new Usuario("Conductor " + n, "1", "c" + n + "@x.com");
			u.altaConductor("LIC-" + n, new Vehiculo("AAA10" + n, "Etios", 4, TipoVehiculo.AUTO,
					CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)));
			todos.add(u);
			vehiculos.addAll(u.getConductor().getVehiculos().values());
		}

		conductorPuro = todos.get(1);
		Servicio estandar = new Servicio("Estandar", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);
		contexto.cargar(todos, vehiculos, List.of(estandar));
	}

	@AfterEach
	void apagar() {

		contexto.close();
		Mensajes.setIdioma("es");
	}

	private String correr(String... lineas) {

		ByteArrayInputStream entrada = new ByteArrayInputStream(
				(String.join("\n", lineas) + "\n").getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream salida = new ByteArrayOutputStream();

		new ControladorConsola(contexto, new VistaConsola(entrada, new PrintStream(salida, true, StandardCharsets.UTF_8)))
				.ejecutar();

		return salida.toString(StandardCharsets.UTF_8);
	}

	@Test
	void unMailQueNoExisteSeInformaYSePuedeSalir() {

		String salida = correr("nadie@x.com", "0");

		assertTrue(salida.contains("No existe ningún usuario con ese email."));
	}

	@Test
	void ingresarComoConductorConUnMailQueNoEsDeConductorMuestraElError() {

		String salida = correr("cliente@x.com", "2", "0", "0");

		assertTrue(salida.contains("Este mail no pertenece a un conductor."));
		assertFalse(salida.contains("Menú de conductor"));
	}

	@Test
	void unConductorVeLasTablasDeDatos() {

		String salida = correr("c1@x.com", "2", "2", "1", "2", "3", "4", "0", "0", "0", "0");

		assertTrue(salida.contains("AAA101"), "tabla de vehiculos");
		assertTrue(salida.contains("cliente@x.com"), "tabla de usuarios");
		assertTrue(salida.contains("Estandar"), "tabla de servicios");
		assertTrue(salida.contains("Patente"), "encabezados");
	}

	@Test
	void unClientePideUnViajeYLoCancela() {

		// 1 usuario, 1 solicitar, 1 primer servicio, 1 pedir, Enter deja de seguirlo, 3 cancelar con motivo
		String salida = correr("cliente@x.com", "1", "1", "1", "1", "", "3", "Cambie de idea", "0", "0", "0");

		assertTrue(salida.contains("Viaje solicitado"), salida);

		EstadoViaje estado = cliente.getCliente().getViajes().getLast().estadoActual();
		assertEquals(EstadoViaje.CANCELADO, estado);
		assertEquals("Cambie de idea", cliente.getCliente().getViajes().getLast().getMotivoCancelacion());
	}

	@Test
	void sinConductoresParaElServicioLaReservaSeRechazaAlInstante() {

		// el unico conductor que atiende el servicio es el que ingresa como cliente? aca no hay ninguno libre:
		contexto.cargar(contexto.getUsuarios(), contexto.getVehiculos(), List.of(new Servicio("Moto", 50, 5, 1,
				TipoVehiculo.MOTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS)));

		String salida = correr("cliente@x.com", "1", "1", "1", "1", "0", "0", "0");

		assertTrue(salida.contains("La reserva fue rechazada"), salida);
		assertEquals(EstadoViaje.RECHAZADO, cliente.getCliente().getViajes().getLast().estadoActual());
	}

	@Test
	void elIdiomaSeCambiaDesdeLaConsola() {

		String salida = correr("i", "0");

		assertEquals("en", Mensajes.getIdioma().getLanguage());
		assertTrue(salida.contains("Language: English"), salida);
	}

	@Test
	void sinEntradaLaConsolaTerminaSinFallar() {

		correr(); // solo una linea en blanco y fin de entrada
	}

	@Test
	void conductorPuroNoTieneViajesAlPrincipio() {

		String salida = correr(conductorPuro.getEmail(), "2", "1", "0", "0", "0");

		assertTrue(salida.contains("Menú de conductor"));
	}

	/** Un viaje del cliente (conductor c1) ya terminado, sin calificar. */
	private Viaje viajeFinalizado() {

		Viaje viaje = new Viaje(cliente, new ubicacion.Ubicacion(-42.77, -65.04), new ubicacion.Ubicacion(-42.76, -65.03),
				contexto.getServicios().get(0));
		viaje.solicitar(LocalDateTime.now());
		viaje.aceptar(LocalDateTime.now(), contexto.getUsuarios().get(1));
		viaje.iniciar(LocalDateTime.now());
		viaje.finalizar(LocalDateTime.now(), CalificacionViaje.NO_CALIFICADO);
		return viaje;
	}

	@Test
	void despuesDeUnViajeExitosoLaConsolaExigeCalificarYNoAceptaValoresInvalidos() {

		Viaje viaje = viajeFinalizado();

		String salida = correr("cliente@x.com", "1", "9", "abc", "0", "4", "Muy amable", "0", "0", "0");

		assertTrue(salida.contains("Tenés que elegir un número del 1 al 5."), salida);
		assertEquals(CalificacionViaje.MUY_BUENO, viaje.getCalificacionConductor());
		assertEquals("Muy amable", viaje.getComentarioCalificacion());
		assertTrue(salida.contains("Menú de usuario"), "recien con la nota se llega al menu");
	}

	@Test
	void conViajeSinCalificarNoSePuedePedirOtro() {

		viajeFinalizado();
		Usuario usuario = cliente;

		assertThrows(IllegalStateException.class, () -> contexto.pedirViaje(usuario, contexto.getServicios().get(0),
				new ubicacion.Ubicacion(-42.77, -65.04), new ubicacion.Ubicacion(-42.76, -65.03)));
	}

	@Test
	void elConductorVePrimeroElRegistroDeSusViajesConLaNota() {

		Viaje viaje = viajeFinalizado();
		viaje.calificarConductor(CalificacionViaje.MUY_BUENO, "Excelente trato");

		String salida = correr("c1@x.com", "2", "0", "0", "0");

		int registro = salida.indexOf("--- Viajes realizados ---");
		assertTrue(registro >= 0 && registro < salida.indexOf("Menú de conductor"), salida);
		assertTrue(salida.contains("★★★★☆"), salida);
		assertTrue(salida.contains("Excelente trato"), salida);
		assertTrue(salida.contains("Cliente"), salida);
		assertTrue(salida.contains("Calificación promedio: 4.00"), salida);
	}
}
