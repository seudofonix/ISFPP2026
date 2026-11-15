package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import servicio.Servicio;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
import viajes.Viaje;
import ui.Formato;

/** El cliente debe calificar al conductor cuando el viaje termina bien. */
class CalificacionViajeTest {

	private Usuario cliente;
	private Usuario conductor;
	private Viaje viaje;

	@BeforeEach
	void preparar() {

		Servicio servicio = new Servicio("Estandar", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);
		cliente = new Usuario("Cliente", "1", "cliente@x.com");
		conductor = new Usuario("Carlos", "1", "carlos@x.com");
		conductor.altaConductor("LIC", new Vehiculo("AA123BB", "Corolla", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));
		conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);
		conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
		viaje = new Viaje(cliente, new Ubicacion(-42.76, -65.03), new Ubicacion(-42.78, -65.05), servicio);
	}

	private void finalizar() {

		viaje.solicitar(LocalDateTime.now());
		viaje.aceptar(LocalDateTime.now(), conductor);
		viaje.iniciar(LocalDateTime.now());
		viaje.finalizar(LocalDateTime.now(), CalificacionViaje.NO_CALIFICADO);
	}

	@Test
	void unViajeFinalizadoQuedaPendienteDeCalificacion() {

		finalizar();

		assertTrue(viaje.requiereCalificacion());
		assertEquals(viaje, cliente.getCliente().viajePendienteDeCalificacion().orElseThrow());
	}

	@Test
	void calificarGuardaLaNotaYLiberaAlCliente() {

		finalizar();
		viaje.calificarConductor(CalificacionViaje.MUY_BUENO);

		assertEquals(CalificacionViaje.MUY_BUENO, viaje.getCalificacionConductor());
		assertFalse(viaje.requiereCalificacion());
		assertTrue(cliente.getCliente().viajePendienteDeCalificacion().isEmpty());
		assertEquals("★★★★☆", Formato.estrellas(viaje.getCalificacionConductor()));
	}

	@Test
	void noSeAceptaSinNotaNiDosVecesNiEnUnViajeSinFinalizar() {

		viaje.solicitar(LocalDateTime.now());
		assertThrows(IllegalStateException.class, () -> viaje.calificarConductor(CalificacionViaje.BUENO));

		viaje.aceptar(LocalDateTime.now(), conductor);
		viaje.iniciar(LocalDateTime.now());
		viaje.finalizar(LocalDateTime.now(), CalificacionViaje.NO_CALIFICADO);

		assertThrows(IllegalArgumentException.class, () -> viaje.calificarConductor(null));
		assertThrows(IllegalArgumentException.class,
				() -> viaje.calificarConductor(CalificacionViaje.NO_CALIFICADO));

		viaje.calificarConductor(CalificacionViaje.BUENO);
		assertThrows(IllegalStateException.class, () -> viaje.calificarConductor(CalificacionViaje.MALO));
	}

	@Test
	void unViajeCanceladoNoPideCalificacion() {

		viaje.solicitar(LocalDateTime.now());
		viaje.cancelar(LocalDateTime.now(), cliente, "motivo");

		assertFalse(viaje.requiereCalificacion());
	}

	@Test
	void elPromedioIgnoraLosViajesSinCalificar() {

		finalizar();
		assertEquals(-1, Formato.promedio(java.util.List.of(viaje)));

		viaje.calificarConductor(CalificacionViaje.EXCELENTE);
		assertEquals(5.0, Formato.promedio(java.util.List.of(viaje)), 1e-9);
	}

	@Test
	void elComentarioEsOpcionalSeLimpiaYTieneLargoMaximo() {

		finalizar();

		assertEquals("", viaje.getComentarioCalificacion());
		assertThrows(IllegalArgumentException.class,
				() -> viaje.calificarConductor(CalificacionViaje.BUENO, "x".repeat(Viaje.MAX_COMENTARIO + 1)));
		assertTrue(viaje.requiereCalificacion(), "un comentario invalido no consume la calificacion");

		viaje.calificarConductor(CalificacionViaje.BUENO, "  Llego rapido  ");

		assertEquals("Llego rapido", viaje.getComentarioCalificacion());
	}
}
