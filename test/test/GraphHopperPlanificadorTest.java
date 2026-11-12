package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rutas.api.RutaCalculada;
import rutas.api.RutaNoDisponibleException;
import rutas.impl.GraphHopperPlanificador;
import ubicacion.Ubicacion;
import vehiculo.TipoVehiculo;

/**
 * Prueba de integracion con GraphHopper real sobre un mapa OSM sintetico: grilla de 8x8 calles (~110 m), la fila
 * 3 es de mano unica hacia el este, y una calle aislada ("isla") sin conexion con el resto.
 */
class GraphHopperPlanificadorTest {

	@TempDir
	static Path carpeta;

	static GraphHopperPlanificador motor;

	static String osmSintetico() {

		double lat0 = -42.78;
		double lon0 = -65.06;
		double paso = 0.001;
		int n = 8;
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<osm version=\"0.6\">\n");

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				xml.append(String.format(java.util.Locale.ROOT, "<node id=\"%d\" lat=\"%.6f\" lon=\"%.6f\" version=\"1\"/>\n",
						i * n + j + 1, lat0 + i * paso, lon0 + j * paso));
			}
		}

		for (int k = 0; k < 3; k++) { // isla desconectada
			xml.append(String.format(java.util.Locale.ROOT, "<node id=\"%d\" lat=\"%.6f\" lon=\"%.6f\" version=\"1\"/>\n",
					5000 + k, lat0 + 0.0035, lon0 + 0.020 + k * 0.0005));
		}

		int id = 1000;

		for (int i = 0; i < n; i++) { // este-oeste
			xml.append("<way id=\"").append(id++).append("\" version=\"1\">");
			for (int j = 0; j < n; j++) {
				xml.append("<nd ref=\"").append(i * n + j + 1).append("\"/>");
			}
			xml.append("<tag k=\"highway\" v=\"residential\"/>");
			if (i == 3) {
				xml.append("<tag k=\"oneway\" v=\"yes\"/>");
			}
			xml.append("</way>\n");
		}

		for (int j = 0; j < n; j++) { // norte-sur
			xml.append("<way id=\"").append(id++).append("\" version=\"1\">");
			for (int i = 0; i < n; i++) {
				xml.append("<nd ref=\"").append(i * n + j + 1).append("\"/>");
			}
			xml.append("<tag k=\"highway\" v=\"residential\"/></way>\n");
		}

		xml.append("<way id=\"9000\" version=\"1\"><nd ref=\"5000\"/><nd ref=\"5001\"/><nd ref=\"5002\"/>"
				+ "<tag k=\"highway\" v=\"residential\"/></way>\n</osm>\n");

		return xml.toString();
	}

	@BeforeAll
	static void importar() throws IOException {

		Path osm = carpeta.resolve("grilla.osm");
		Files.writeString(osm, osmSintetico());
		motor = new GraphHopperPlanificador(osm.toString(), carpeta.resolve("cache").toString());
	}

	@AfterAll
	static void cerrar() {

		motor.close();
	}

	@Test
	void calculaUnaRutaSobreLasCallesMasLargaQueLaLineaRecta() {

		Ubicacion a = new Ubicacion(-42.7799, -65.0599);
		Ubicacion b = new Ubicacion(-42.7735, -65.0545);

		RutaCalculada ruta = motor.calcular(a, b, TipoVehiculo.AUTO);

		assertTrue(ruta.distanciaMetros() > a.calcularDistancia(b) * 1000, "por calles es mas largo que en linea recta");
		assertTrue(ruta.duracionMilisegundos() > 0);
		assertTrue(ruta.geometria().size() >= 2);
	}

	@Test
	void respetaLaCalleDeManoUnica() {

		Ubicacion oeste = new Ubicacion(-42.777, -65.058);
		Ubicacion este = new Ubicacion(-42.777, -65.054);

		double alEste = motor.calcular(oeste, este, TipoVehiculo.AUTO).distanciaMetros();
		double alOeste = motor.calcular(este, oeste, TipoVehiculo.AUTO).distanciaMetros();

		assertTrue(alOeste > alEste + 100, "contra la mano hay que rodear: " + alEste + " vs " + alOeste);
	}

	@Test
	void ajustaAUnaCalleCercanaYRechazaLoQueEstaLejos() {

		Optional<Ubicacion> cerca = motor.ajustarACalle(new Ubicacion(-42.7785, -65.0595), 200);
		assertTrue(cerca.isPresent());
		assertEquals(-65.06, cerca.get().getLongitud(), 1e-6); // la calle mas cercana es el borde oeste

		assertFalse(motor.ajustarACalle(new Ubicacion(-42.7600, -65.0600), 100).isPresent());
		assertFalse(motor.ajustarACalle(null, 100).isPresent());
	}

	@Test
	void nuncaAjustaAUnaCalleAisladaDeLaRed() {

		Optional<Ubicacion> ajustada = motor.ajustarACalle(new Ubicacion(-42.7765, -65.0395), 5000);

		assertTrue(ajustada.isPresent());
		assertTrue(ajustada.get().getLongitud() < -65.05, "debe caer en la red principal, no en la isla");
	}

	@Test
	void erroresDelMotorSeTraducenAlContratoPropio() {

		Ubicacion a = new Ubicacion(-42.7799, -65.0599);

		assertThrows(RutaNoDisponibleException.class, () -> motor.calcular(a, new Ubicacion(-10, -10), TipoVehiculo.AUTO));
		assertThrows(RutaNoDisponibleException.class, () -> motor.calcular(a, a, TipoVehiculo.MOTO));
		assertThrows(RutaNoDisponibleException.class, () -> motor.calcular(null, a, TipoVehiculo.AUTO));
		assertEquals(2, motor.calcular(a, a, TipoVehiculo.AUTO).geometria().size()); // mismo punto: 2 puntos iguales
	}

	@Test
	void sinArchivoOsmElMensajeIndicaDondeBuscarlo() {

		RutaNoDisponibleException e = assertThrows(RutaNoDisponibleException.class,
				() -> new GraphHopperPlanificador(carpeta.resolve("no_existe.osm").toString(), carpeta.resolve("c2").toString()));

		assertTrue(e.getMessage().contains("no_existe.osm"));
	}
}
