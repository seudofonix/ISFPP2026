package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import logica.PlanificacionViajeService;
import rutas.api.PuntoRuta;
import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import rutas.api.RutasViaje;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;

class RutasTest {

	private final List<PuntoRuta> dosPuntos = List.of(new PuntoRuta(-42.77, -65.04), new PuntoRuta(-42.76, -65.03));

	@Test
	void rutaCalculadaValidaSusDatosYCopiaLaLista() {

		List<PuntoRuta> mutable = new ArrayList<>(dosPuntos);
		RutaCalculada r = new RutaCalculada(1500, 120_000, mutable);
		mutable.clear();

		assertEquals(2, r.geometria().size());
		assertEquals(1.5, r.distanciaKm(), 1e-9);
		assertEquals(2.0, r.duracionMinutos(), 1e-9);
		assertThrows(UnsupportedOperationException.class, () -> r.geometria().add(dosPuntos.get(0)));

		assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(-1, 0, dosPuntos));
		assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(1, -1, dosPuntos));
		assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(1, 1, List.of(dosPuntos.get(0))));
		assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(1, 1, null));
		assertThrows(IllegalArgumentException.class, () -> new PuntoRuta(Double.NaN, 0));
	}

	@Test
	void rutasViajeExigeLosDosTramos() {

		RutaCalculada r = new RutaCalculada(1, 1, dosPuntos);

		assertThrows(NullPointerException.class, () -> new RutasViaje(r, null));
		assertEquals(r, new RutasViaje(r, r).haciaOrigen());
	}

	@Test
	void planificarCalculaAcercamientoYTrasladoSeparados() {

		Vehiculo v = new Vehiculo("AAA111", "Etios", 4, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				Set.of(TipoServicio.PASAJEROS));
		v.setUbicacion(new Ubicacion(-42.780, -65.050));

		Ubicacion origen = new Ubicacion(-42.770, -65.040);
		Ubicacion destino = new Ubicacion(-42.750, -65.020);

		RutasViaje rutas = new PlanificacionViajeService(new MotorFalso()).planificar(v, origen, destino);

		assertEquals(v.getUbicacion().calcularDistancia(origen) * 1000, rutas.haciaOrigen().distanciaMetros(), 1e-6);
		assertEquals(origen.calcularDistancia(destino) * 1000, rutas.haciaDestino().distanciaMetros(), 1e-6);
	}

	@Test
	void planificarFallaSinUbicacionOSiElPerfilNoEstaImplementado() {

		PlanificacionViajeService servicio = new PlanificacionViajeService(new MotorFalso());
		Vehiculo sinUbicacion = new Vehiculo("AAA111", "Etios", 4, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
				Set.of(TipoServicio.PASAJEROS));

		assertThrows(IllegalStateException.class,
				() -> servicio.planificar(sinUbicacion, new Ubicacion(-42.77, -65.04), new Ubicacion(-42.76, -65.03)));

		Vehiculo moto = new Vehiculo("BBB222", "Wave", 1, TipoVehiculo.MOTO, CategoriaVehiculo.ESTANDAR,
				Set.of(TipoServicio.PASAJEROS));
		moto.setUbicacion(new Ubicacion(-42.78, -65.05));

		assertThrows(RutaNoDisponibleException.class,
				() -> servicio.planificar(moto, new Ubicacion(-42.77, -65.04), new Ubicacion(-42.76, -65.03)));
	}
}
