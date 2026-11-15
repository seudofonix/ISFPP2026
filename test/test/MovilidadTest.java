package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import logica.Asignacion;
import logica.Movilidad;
import servicio.Servicio;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;

class MovilidadTest {

	private final Ubicacion origen = new Ubicacion(-42.770, -65.040);
	private Servicio estandar;
	private Usuario cerca;
	private Usuario lejos;
	private Usuario moto;

	private Usuario conductor(String nombre, String email, Vehiculo v, Ubicacion donde) {

		Usuario u = new Usuario(nombre, "1", email);
		v.setUbicacion(donde);
		u.altaConductor("LIC", v);
		u.cambiarRolActivo(RolUsuario.CONDUCTOR);
		return u;
	}

	@BeforeEach
	void preparar() {

		estandar = new Servicio("Estandar", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);

		cerca = conductor("Cerca", "cerca@x.com", new Vehiculo("AAA111", "Etios", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)), new Ubicacion(-42.771, -65.041));
		lejos = conductor("Lejos", "lejos@x.com", new Vehiculo("BBB222", "Focus", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)), new Ubicacion(-42.800, -65.100));
		moto = conductor("Moto", "moto@x.com", new Vehiculo("CCC333", "Wave", 1, TipoVehiculo.MOTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)), new Ubicacion(-42.7701, -65.0401));
	}

	@Test
	void eligeAlConductorCompatibleMasCercano() {

		Optional<Asignacion> a = Movilidad.seleccionarConductor(List.of(lejos, moto, cerca), estandar, origen);

		assertEquals(cerca, a.orElseThrow().conductor());
		assertEquals("AAA111", a.get().vehiculo().getPatente());
		assertTrue(a.get().distanciaKm() < 0.2);
	}

	@Test
	void ignoraLosVehiculosDeOtroTipoAunqueEstenMasCerca() {

		// la moto esta a pocos metros pero el servicio es de AUTO
		assertEquals(cerca, Movilidad.seleccionarConductor(List.of(moto, cerca), estandar, origen).orElseThrow().conductor());
		assertTrue(Movilidad.seleccionarConductor(List.of(moto), estandar, origen).isEmpty());
	}

	@Test
	void ignoraAQuienNoEstaDisponibleONoTieneUbicacion() {

		cerca.cambiarRolActivo(RolUsuario.CLIENTE); // pasa a cliente: fuera de servicio
		assertEquals(lejos, Movilidad.seleccionarConductor(List.of(cerca, lejos), estandar, origen).orElseThrow().conductor());

		lejos.getConductor().getVehiculos().values().forEach(v -> v.setUbicacion(null));
		assertTrue(Movilidad.seleccionarConductor(List.of(cerca, lejos), estandar, origen).isEmpty());
	}

	@Test
	void sinDatosNoHayAsignacion() {

		assertTrue(Movilidad.seleccionarConductor(List.of(), estandar, origen).isEmpty());
		assertTrue(Movilidad.seleccionarConductor(null, estandar, origen).isEmpty());
		assertTrue(Movilidad.seleccionarConductor(List.of(cerca), null, origen).isEmpty());
	}

	@Test
	void unServicioDeEnviosNoLoPrestaUnVehiculoSoloDePasajeros() {

		Servicio envios = new Servicio("Envios", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.ENVIOS);

		assertTrue(Movilidad.seleccionarConductor(List.of(cerca, lejos), envios, origen).isEmpty());
	}

	@Test
	void unVehiculoDeMayorCategoriaPuedePrestarUnServicioInferior() {

		Usuario premium = conductor("Premium", "p@x.com", new Vehiculo("DDD444", "BMW", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.PREMIUM, Set.of(TipoServicio.PASAJEROS)), new Ubicacion(-42.7701, -65.0401));

		assertEquals(premium, Movilidad.seleccionarConductor(List.of(premium), estandar, origen).orElseThrow().conductor());

		Servicio confortPremium = new Servicio("Premium", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM,
				TipoServicio.PASAJEROS);
		assertTrue(Movilidad.seleccionarConductor(List.of(cerca), confortPremium, origen).isEmpty());
	}

	@Test
	void unConductorPremiumTomaConfortPeroUnoConfortNoTomaPremium() {

		Servicio confort = new Servicio("Confort", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.CONFORT,
				TipoServicio.PASAJEROS);
		Servicio premium = new Servicio("Premium", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM,
				TipoServicio.PASAJEROS);
		Usuario conductorPremium = conductor("Premium", "p@x.com", new Vehiculo("DDD444", "BMW", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.PREMIUM, Set.of(TipoServicio.PASAJEROS)), new Ubicacion(-42.7701, -65.0401));
		Usuario conductorConfort = conductor("Confort", "c@x.com", new Vehiculo("EEE555", "Cruze", 4,
				TipoVehiculo.AUTO, CategoriaVehiculo.CONFORT, Set.of(TipoServicio.PASAJEROS)),
				new Ubicacion(-42.7701, -65.0401));

		assertTrue(Movilidad.seleccionarConductor(List.of(conductorPremium), confort, origen).isPresent());
		assertTrue(Movilidad.seleccionarConductor(List.of(conductorConfort), confort, origen).isPresent());
		assertTrue(Movilidad.seleccionarConductor(List.of(conductorConfort), premium, origen).isEmpty());
	}

	@Test
	void losConductoresSimuladosCubrenTodosLosServiciosAunSiSonPocos() {

		Servicio confort = new Servicio("Confort", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.CONFORT,
				TipoServicio.PASAJEROS);
		Servicio premium = new Servicio("Premium", 100, 10, 1, TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM,
				TipoServicio.PASAJEROS);
		Usuario conductorPremium = conductor("Premium", "zzz@x.com", new Vehiculo("DDD444", "BMW", 4,
				TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM, Set.of(TipoServicio.PASAJEROS)),
				new Ubicacion(-42.7701, -65.0401));

		// por email "cerca" y "lejos" van antes que "zzz", pero el premium es el unico que cubre los tres servicios
		List<Usuario> elegidos = Movilidad.elegirConductoresSimulados(List.of(cerca, lejos, conductorPremium),
				List.of(estandar, confort, premium), null, 2);

		assertEquals(2, elegidos.size());
		assertEquals(conductorPremium, elegidos.get(0));

		// el usuario de la sesion nunca se simula
		assertTrue(Movilidad.elegirConductoresSimulados(List.of(cerca, conductorPremium),
				List.of(estandar, confort, premium), conductorPremium, 3).stream()
				.noneMatch(u -> u.equals(conductorPremium)));
	}

	@Test
	void vehiculoCompatibleDelConductorEligeElDeMenorCategoria() {

		Usuario u = new Usuario("Mixto", "1", "mixto@x.com");
		u.altaConductor("LIC", new Vehiculo("ZZZ999", "BMW", 4, TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM,
				Set.of(TipoServicio.PASAJEROS)));
		u.getConductor().agregarVehiculo(new Vehiculo("YYY888", "Etios", 4, TipoVehiculo.AUTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)));
		u.getConductor().agregarVehiculo(new Vehiculo("XXX777", "Wave", 1, TipoVehiculo.MOTO,
				CategoriaVehiculo.ESTANDAR, Set.of(TipoServicio.PASAJEROS)));

		assertEquals("YYY888", u.getConductor().vehiculoCompatible(estandar).getPatente());
		assertNull(u.getConductor().vehiculoCompatible(null));
	}
}
