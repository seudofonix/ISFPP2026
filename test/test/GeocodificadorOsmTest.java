package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rutas.impl.GeocodificadorOsm;
import ubicacion.Ubicacion;

/** Calle y altura de un punto a partir del nombre de calle del motor y los domicilios del extracto OSM. */
class GeocodificadorOsmTest {

	private static final String OSM = """
			<?xml version='1.0' encoding='UTF-8'?>
			<osm version="0.6">
			 <node id="1" lat="-42.7700" lon="-65.0400"><tag k="addr:street" v="Avenida Roca"/><tag k="addr:housenumber" v="100"/></node>
			 <node id="2" lat="-42.7710" lon="-65.0400"><tag k="addr:street" v="Avenida Roca"/><tag k="addr:housenumber" v="200"/></node>
			 <node id="3" lat="-42.7800" lon="-65.0300"><tag k="addr:street" v="Avenida Güemes"/><tag k="addr:housenumber" v="45B"/></node>
			 <node id="4" lat="-42.7900" lon="-65.0300"><tag k="addr:street" v="Lavalle"/><tag k="addr:housenumber" v="s/n"/></node>
			 <node id="5" lat="-42.7950" lon="-65.0300"><tag k="name" v="Sin domicilio"/></node>
			 <way id="9"><nd ref="1"/><nd ref="2"/><tag k="addr:street" v="Calle Falsa"/><tag k="addr:housenumber" v="999"/></way>
			</osm>
			""";

	private MotorFalso motor;
	private GeocodificadorOsm geocodificador;

	@BeforeEach
	void preparar(@TempDir Path carpeta) throws IOException {

		Path archivo = carpeta.resolve("prueba.osm");
		Files.writeString(archivo, OSM);
		motor = new MotorFalso();
		geocodificador = new GeocodificadorOsm(motor, archivo.toString());
	}

	@Test
	void usaElNumeroDelDomicilioCuandoElPuntoEstaMuyCerca() {

		motor.calle = "Avenida Roca";

		assertEquals("Avenida Roca 100", geocodificador.direccion(new Ubicacion(-42.77005, -65.0400)));
	}

	@Test
	void interpolaLaAlturaEntreLosDosDomiciliosMasCercanos() {

		motor.calle = "Avenida Roca";

		assertEquals("Avenida Roca al 150", geocodificador.direccion(new Ubicacion(-42.7705, -65.0400)));
	}

	@Test
	void ignoraAcentosYMayusculasAlRelacionarLaCalleConSusDomicilios() {

		motor.calle = "AVENIDA GUEMES";

		assertEquals("AVENIDA GUEMES 45", geocodificador.direccion(new Ubicacion(-42.7800, -65.0300)));
	}

	@Test
	void sinDomiciliosEnLaCalleInformaSinNumero() {

		motor.calle = "Lavalle"; // su unico domicilio es "s/n": no sirve para estimar

		assertEquals("Lavalle s/n", geocodificador.direccion(new Ubicacion(-42.7900, -65.0300)));
	}

	@Test
	void unDomicilioMuyLejanoNoSeUsaParaEstimar() {

		motor.calle = "Avenida Roca";

		assertEquals("Avenida Roca s/n", geocodificador.direccion(new Ubicacion(-42.85, -65.10)));
	}

	@Test
	void sinNombreDeCalleUsaElDomicilioCercanoOInformaCalleSinNombre() {

		motor.calle = null;

		assertEquals("Avenida Roca 100", geocodificador.direccion(new Ubicacion(-42.77003, -65.0400)));
		assertEquals("Calle sin nombre", geocodificador.direccion(new Ubicacion(-42.90, -65.20)));
		assertEquals("Calle sin nombre", geocodificador.direccion(null));
	}

	@Test
	void siNoSePuedeLeerElArchivoSeInformaSoloLaCalle() {

		motor.calle = "Avenida Roca";
		GeocodificadorOsm sinArchivo = new GeocodificadorOsm(motor, "no-existe.osm");

		assertEquals("Avenida Roca s/n", sinArchivo.direccion(new Ubicacion(-42.7700, -65.0400)));
	}
}
