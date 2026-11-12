/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Adaptador de GraphHopper al contrato PlanificadorRutas. Importa el extracto OpenStreetMap (.osm), arma el
 * grafo dirigido (perfil "car" con costos de giro y preparacion CH) y lo guarda en una cache para las
 * proximas ejecuciones. Convierte todo lo de GraphHopper a los tipos neutrales de rutas.api.
 * Perfil implementado: AUTO. MOTO queda pendiente.
 */
package rutas.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.TurnCostsConfig;
import com.graphhopper.util.shapes.GHPoint3D;

import rutas.api.PlanificadorRutas;
import rutas.api.PuntoRuta;
import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import ubicacion.Ubicacion;
import vehiculo.TipoVehiculo;

public final class GraphHopperPlanificador implements PlanificadorRutas {

	private static final String PERFIL_AUTO = "car";

	private final GraphHopper hopper;
	private final EdgeFilter filtroAuto;

	/**
	 * @param archivoOsm      extracto OSM (.osm o .osm.pbf)
	 * @param directorioCache carpeta donde GraphHopper guarda el grafo importado
	 */
	public GraphHopperPlanificador(String archivoOsm, String directorioCache) {

		File osm = new File(archivoOsm);

		if (!osm.isFile()) {

			throw new RutaNoDisponibleException("No se encontro el extracto vial " + osm.getAbsolutePath()
					+ ". Copialo en esa ruta o ajusta 'rutas.osm' en config.properties.");
		}

		GraphHopper gh = new GraphHopper();
		gh.setOSMFile(osm.getPath());
		gh.setGraphHopperLocation(directorioCache);
		gh.setEncodedValuesString("car_access, car_average_speed");

		CustomModel modeloAuto = GHUtility.loadCustomModelFromJar("car.json");
		gh.setProfiles(new Profile(PERFIL_AUTO).setCustomModel(modeloAuto).setTurnCostsConfig(TurnCostsConfig.car()));
		gh.getCHPreparationHandler().setCHProfiles(new CHProfile(PERFIL_AUTO));

		try {

			gh.importOrLoad();

		} catch (RuntimeException e) {

			throw new RutaNoDisponibleException("No se pudo importar el mapa vial " + osm.getPath() + ": "
					+ e.getMessage(), e);
		}

		this.hopper = gh;

		Weighting ponderacion = gh.createWeighting(gh.getProfile(PERFIL_AUTO), new PMap());
		BooleanEncodedValue subred = gh.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(PERFIL_AUTO));
		this.filtroAuto = new DefaultSnapFilter(ponderacion, subred);
	}

	@Override
	public RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo) {

		if (origen == null || destino == null) {

			throw new RutaNoDisponibleException("Faltan el origen o el destino de la ruta.");
		}

		if (vehiculo != TipoVehiculo.AUTO) {

			throw new RutaNoDisponibleException(
					"El perfil de circulacion para " + vehiculo + " todavia no esta implementado (solo AUTO).");
		}

		GHRequest pedido = new GHRequest(origen.getLatitud(), origen.getLongitud(), destino.getLatitud(),
				destino.getLongitud()).setProfile(PERFIL_AUTO);

		GHResponse respuesta = hopper.route(pedido);

		if (respuesta.hasErrors()) {

			throw new RutaNoDisponibleException("No hay ruta vial entre los puntos: "
					+ respuesta.getErrors().get(0).getMessage(), respuesta.getErrors().get(0));
		}

		ResponsePath camino = respuesta.getBest();
		List<PuntoRuta> geometria = convertir(camino.getPoints());

		if (geometria.size() == 1) { // origen y destino sobre el mismo punto de la red

			geometria.add(geometria.get(0));
		}

		return new RutaCalculada(camino.getDistance(), camino.getTime(), geometria);
	}

	@Override
	public Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros) {

		if (candidata == null) {

			return Optional.empty();
		}

		Snap snap = hopper.getLocationIndex().findClosest(candidata.getLatitud(), candidata.getLongitud(),
				filtroAuto);

		if (!snap.isValid() || snap.getQueryDistance() > maxDistanciaMetros) {

			return Optional.empty();
		}

		GHPoint3D punto = snap.getSnappedPoint();

		return Optional.of(new Ubicacion(redondear(punto.getLat()), redondear(punto.getLon())));
	}

	@Override
	public void close() {

		hopper.close();
	}

	private List<PuntoRuta> convertir(PointList puntos) {

		List<PuntoRuta> resultado = new ArrayList<>(puntos.size());

		for (int i = 0; i < puntos.size(); i++) {

			resultado.add(new PuntoRuta(puntos.getLat(i), puntos.getLon(i)));
		}

		return resultado;
	}

	private double redondear(double valor) {

		return Math.round(valor * 1_000_000.0) / 1_000_000.0;
	}
}
