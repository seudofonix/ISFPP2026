package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import datos.Datos;
import datos.factory.FabricaLectores;
import servicio.Servicio;
import servicio.TipoServicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

class DatosFactoryTest {

	@TempDir
	Path dir;

	private String escribir(String nombre, String contenido) throws IOException {

		Path archivo = dir.resolve(nombre);
		Files.writeString(archivo, contenido);
		return archivo.toString();
	}

	private String archivoVehiculos() throws IOException {

		return escribir("v.txt", "# encabezado\nAB123CD;Etios;4;AUTO;ESTANDAR;PASAJEROS;ENVIOS\n\nXY1;Wave;1;MOTO;ESTANDAR;ENVIOS\nlinea;corta\n");
	}

	@Test
	void cargaVehiculosIgnorandoComentariosVaciasYLineasCortas() throws IOException {

		HashMap<String, Vehiculo> vehiculos = Datos.cargarVehiculos(archivoVehiculos());

		assertEquals(2, vehiculos.size());
		assertTrue(vehiculos.get("AB123CD").getTipoServicio().contains(TipoServicio.ENVIOS));
		assertEquals(1, vehiculos.get("XY1").getCapacidadPasajeros());
	}

	@Test
	void usuariosConLicenciaSonConductoresConSusVehiculos() throws IOException {

		HashMap<String, Vehiculo> vehiculos = Datos.cargarVehiculos(archivoVehiculos());
		String archivo = escribir("u.txt", "Juan;111;juan@x.com\nMaria;222;maria@x.com;LIC1;AB123CD;XY1;NOEXISTE\n");

		HashMap<String, Usuario> usuarios = Datos.cargarUsuarios(archivo, vehiculos);

		assertNull(usuarios.get("juan@x.com").getConductor());
		assertNotNull(usuarios.get("maria@x.com").getConductor());
		assertEquals(2, usuarios.get("maria@x.com").getConductor().getVehiculos().size());
	}

	@Test
	void cargaServiciosYCalculaCosto() throws IOException {

		String archivo = escribir("s.txt", "# x\nViaje;1000;350;50;AUTO;ESTANDAR;PASAJEROS\n");

		ArrayList<Servicio> servicios = Datos.cargarServicios(archivo);

		assertEquals(1, servicios.size());
		assertEquals(1000 + 10 * 350 + 20 * 50, servicios.get(0).calcularCosto(10, 20), 0.0001);
	}

	@Test
	void datosConFormatoInvalidoIndicanLaLinea() throws IOException {

		String archivo = escribir("malo.txt", "X;M;4;NAVE;ESTANDAR\n");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Datos.cargarVehiculos(archivo));
		assertTrue(e.getMessage().contains("linea 1"));
	}

	@Test
	void archivoInexistenteLanzaFileNotFound() {

		assertThrows(FileNotFoundException.class, () -> Datos.cargarServicios(dir.resolve("no.txt").toString()));
	}

	@Test
	void cadaSubFabricaCreaSuPropioLector() {

		assertNotNull(FabricaLectores.vehiculos().crearLector());
		assertNotNull(FabricaLectores.usuarios(null).crearLector());
		assertNotNull(FabricaLectores.servicios().crearLector());
	}
}
