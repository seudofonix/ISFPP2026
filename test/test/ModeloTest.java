package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.Cotizacion;
import modelo.Modelo;
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

class ModeloTest {

	private Modelo modelo;
	private Usuario cliente;
	private Usuario conductor;
	private Servicio servicio;

	private final Ubicacion plaza = new Ubicacion(-42.770, -65.040);
	private final Ubicacion muelle = new Ubicacion(-42.760, -65.030);

	@BeforeEach
	void preparar() {

		modelo = new Modelo();

		cliente = new Usuario("Maria Gomez", "111", "maria@x.com");
		conductor = new Usuario("Juan Perez", "222", "juan@x.com");
		conductor.altaConductor("LIC-1",
				new Vehiculo("AB123CD", "Corolla", 4, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
						Set.of(TipoServicio.PASAJEROS)));

		servicio = new Servicio("Estandar", 2000, 200, 100, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				TipoServicio.PASAJEROS);

		modelo.getUsuarios().addAll(cliente, conductor);
		modelo.getServicios().add(servicio);
	}

	@Test
	void cotizarUsaHaversineYLaVelocidadMedia() {

		Cotizacion c = modelo.cotizar(servicio, plaza, muelle);

		double km = plaza.calcularDistancia(muelle);
		double minutos = km / 40.0 * 60.0; // velocidad media por defecto: 40 km/h

		assertEquals(km, c.distanciaKm(), 1e-9);
		assertEquals(minutos, c.duracionMinutos(), 1e-9);
		assertEquals(servicio.calcularCosto(km, minutos), c.costo(), 1e-9);
		assertTrue(c.costo() > servicio.getTarifaBase());
	}

	@Test
	void cotizarRechazaDatosInvalidos() {

		assertThrows(IllegalArgumentException.class, () -> modelo.cotizar(null, plaza, muelle));
		assertThrows(IllegalArgumentException.class, () -> modelo.cotizar(servicio, plaza, plaza));
		assertThrows(IllegalArgumentException.class, () -> modelo.cotizar(servicio, null, muelle));

		Ubicacion fueraDelArea = new Ubicacion(40.0, -3.0);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> modelo.cotizar(servicio, fueraDelArea, muelle));
		assertTrue(e.getMessage().contains("fuera del area"));
	}

	@Test
	void ubicacionAleatoriaSiempreCaeDentroDelArea() {

		for (int i = 0; i < 200; i++) {

			Ubicacion u = modelo.generarUbicacionAleatoria();
			modelo.cotizar(servicio, u, muelle.equals(u) ? plaza : muelle); // no debe lanzar "fuera del area"
		}
	}

	@Test
	void reservarCreaViajeSolicitadoYLoAgregaALasReservas() {

		modelo.iniciarSesion(cliente, RolUsuario.CLIENTE);

		Viaje v = modelo.reservarViaje(servicio, plaza, muelle);

		assertEquals(EstadoViaje.SOLICITADO, v.estadoActual());
		assertEquals(plaza, v.getOrigen());
		assertEquals(muelle, v.getDestino());
		assertEquals(List.of(v), modelo.getReservas());
		assertEquals(List.of(v), cliente.getCliente().getViajes());
	}

	@Test
	void noSePuedeReservarSinSesionDeClienteNiConOtroViajeActivo() {

		assertThrows(IllegalStateException.class, () -> modelo.reservarViaje(servicio, plaza, muelle));

		modelo.iniciarSesion(conductor, RolUsuario.CONDUCTOR);
		assertThrows(IllegalStateException.class, () -> modelo.reservarViaje(servicio, plaza, muelle));
		modelo.cerrarSesion();

		modelo.iniciarSesion(cliente, RolUsuario.CLIENTE);
		modelo.reservarViaje(servicio, plaza, muelle);
		assertThrows(IllegalStateException.class, () -> modelo.reservarViaje(servicio, plaza, muelle));
		assertEquals(1, modelo.getReservas().size());
	}

	@Test
	void cancelarLiberaAlClienteParaReservarDeNuevo() {

		modelo.iniciarSesion(cliente, RolUsuario.CLIENTE);
		Viaje v = modelo.reservarViaje(servicio, plaza, muelle);

		modelo.cancelarReserva(v);

		assertEquals(EstadoViaje.CANCELADO, v.estadoActual());
		assertThrows(IllegalStateException.class, () -> modelo.cancelarReserva(v)); // ya cancelada

		Viaje otro = modelo.reservarViaje(servicio, muelle, plaza);
		assertEquals(EstadoViaje.SOLICITADO, otro.estadoActual());
		assertEquals(2, modelo.getReservas().size());
	}

	@Test
	void lasReservasSeConservanAlVolverAIngresar() {

		modelo.iniciarSesion(cliente, RolUsuario.CLIENTE);
		Viaje v = modelo.reservarViaje(servicio, plaza, muelle);
		modelo.cerrarSesion();

		assertTrue(modelo.getReservas().isEmpty());
		assertNull(modelo.getUsuarioActual());

		modelo.iniciarSesion(cliente, RolUsuario.CLIENTE);
		assertEquals(List.of(v), modelo.getReservas());
	}

	@Test
	void ingresoComoConductorRespetaLasReglasDelDominio() {

		assertThrows(IllegalStateException.class, () -> modelo.iniciarSesion(cliente, RolUsuario.CONDUCTOR));
		assertNull(modelo.getUsuarioActual());

		modelo.iniciarSesion(conductor, RolUsuario.CONDUCTOR);
		assertEquals(RolUsuario.CONDUCTOR, conductor.getRolActivo());
		assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
		assertNotNull(modelo.getUsuarioActual());

		modelo.cerrarSesion();
		assertEquals(RolUsuario.CLIENTE, conductor.getRolActivo());
		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, conductor.getConductor().getEstadoConductor());
	}

	@Test
	void ingresarDosVecesComoConductorSinCerrarSesionNoFalla() {

		modelo.iniciarSesion(conductor, RolUsuario.CONDUCTOR);
		modelo.iniciarSesion(conductor, RolUsuario.CONDUCTOR);

		assertEquals(RolUsuario.CONDUCTOR, conductor.getRolActivo());
	}

	@Test
	void listaDeUsuariosSegunElRol() {

		assertEquals(2, modelo.getUsuariosConRol(RolUsuario.CLIENTE).size());
		assertEquals(List.of(conductor), modelo.getUsuariosConRol(RolUsuario.CONDUCTOR));
	}
}
