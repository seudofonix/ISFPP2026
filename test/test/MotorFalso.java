package test;

import java.util.List;
import java.util.Optional;

import rutas.api.PlanificadorRutas;
import rutas.api.PuntoRuta;
import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import ubicacion.Ubicacion;
import vehiculo.TipoVehiculo;

/** Motor de rutas de prueba: ruta en linea recta y ajuste a calle configurable. */
public class MotorFalso implements PlanificadorRutas {

	public boolean hayCalles = true;
	public boolean cerrado = false;

	@Override
	public RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo) {

		if (vehiculo != TipoVehiculo.AUTO) {

			throw new RutaNoDisponibleException("perfil " + vehiculo + " pendiente");
		}

		double metros = origen.calcularDistancia(destino) * 1000;

		return new RutaCalculada(metros, (long) (metros * 100), List.of(new PuntoRuta(origen.getLatitud(),
				origen.getLongitud()), new PuntoRuta(destino.getLatitud(), destino.getLongitud())));
	}

	@Override
	public Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros) {

		return hayCalles ? Optional.of(candidata) : Optional.empty();
	}

	@Override
	public void close() {

		cerrado = true;
	}
}
