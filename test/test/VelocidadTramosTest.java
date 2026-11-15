package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import aplicacion.ContextoAplicacion;
import datos.CargarParametros;
import logica.DuracionTramos;
import servicio.Servicio;
import servicio.TipoServicio;
import simulacion.GeneradorUbicacionesViales;
import ubicacion.Ubicacion;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.EstadoViaje;
import viajes.Viaje;

/** La recogida (conductor -> origen) se recorre a la misma velocidad que el traslado (origen -> destino). */
class VelocidadTramosTest {

	private ContextoAplicacion contexto;
	private Usuario cliente;
	private Usuario conductor;
	private Vehiculo vehiculo;
	private Servicio servicio;

	@BeforeEach
	void preparar() {

		MotorFalso motor = new MotorFalso();
		contexto = new ContextoAplicacion(motor, new GeneradorUbicacionesViales(motor, new Random(1)));
		servicio = new Servicio("Estandar", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);
		cliente = new Usuario("Cliente", "1", "cliente@x.com");
		conductor = new Usuario("Conductor", "1", "c@x.com");
		vehiculo = new Vehiculo("AAA111", "Etios", 4, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);
		conductor.altaConductor("LIC", vehiculo);
		contexto.cargar(List.of(cliente, conductor), List.of(vehiculo), List.of(servicio));
	}

	@AfterEach
	void apagar() {

		contexto.close();
	}

	private Viaje viajeAceptado(Ubicacion posicionConductor, Ubicacion origen, Ubicacion destino) {

		vehiculo.setUbicacion(posicionConductor);
		Viaje viaje = new Viaje(cliente, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());
		viaje.aceptar(LocalDateTime.now(), conductor);
		return viaje;
	}

	@Test
	void laRecogidaDuraProporcionalASuDistanciaALaVelocidadDelTraslado() {

		Ubicacion origen = new Ubicacion(-42.770, -65.040);
		Ubicacion destino = new Ubicacion(-42.760, -65.040); // traslado ~1.1 km
		Ubicacion lejos = new Ubicacion(-42.790, -65.040); // recogida ~2.2 km (el doble)

		Viaje viaje = viajeAceptado(lejos, origen, destino);
		double base = CargarParametros.getSegundosTramo();

		assertEquals(base, contexto.segundosDelTramo(viaje, EstadoViaje.INICIADO), 1e-9);
		assertEquals(base * 2, contexto.segundosDelTramo(viaje, EstadoViaje.ACEPTADO), base * 0.01);

		// misma velocidad: km / segundos iguales en los dos tramos
		double vTraslado = origen.calcularDistancia(destino) / contexto.segundosDelTramo(viaje, EstadoViaje.INICIADO);
		double vRecogida = lejos.calcularDistancia(origen) / contexto.segundosDelTramo(viaje, EstadoViaje.ACEPTADO);
		assertEquals(vTraslado, vRecogida, vTraslado * 0.01);
	}

	@Test
	void unConductorCercaTardaMenosQueUnoLejos() {

		Ubicacion origen = new Ubicacion(-42.770, -65.040);
		Ubicacion destino = new Ubicacion(-42.760, -65.040);

		Viaje cerca = viajeAceptado(new Ubicacion(-42.771, -65.040), origen, destino);
		double segundosCerca = contexto.segundosDelTramo(cerca, EstadoViaje.ACEPTADO);

		assertTrue(segundosCerca < CargarParametros.getSegundosTramo());
		assertTrue(segundosCerca >= DuracionTramos.MINIMO_SEGUNDOS);
	}

	@Test
	void formulaPuraConPisoYSinDistanciaDeReferencia() {

		assertEquals(8.0, DuracionTramos.recogida(2.0, 1.0, 4.0), 1e-9);
		assertEquals(2.0, DuracionTramos.recogida(0.5, 1.0, 4.0), 1e-9);
		assertEquals(DuracionTramos.MINIMO_SEGUNDOS, DuracionTramos.recogida(0.0, 1.0, 4.0), 1e-9);
		assertEquals(4.0, DuracionTramos.recogida(2.0, 0.0, 4.0), 1e-9);
	}
}
