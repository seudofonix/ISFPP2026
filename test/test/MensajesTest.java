package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import idioma.Mensajes;
import viajes.EstadoViaje;

/** Internacionalizacion: los dos idiomas tienen las mismas claves y no falta ninguna de las que usa el codigo. */
class MensajesTest {

	@AfterEach
	void volverAEspanol() {

		Mensajes.setIdioma("es");
	}

	private static Set<String> claves(String idioma) {

		Mensajes.setIdioma(idioma);
		return new TreeSet<>(Set.copyOf(Mensajes.getBundle().keySet()));
	}

	@Test
	void espanolEInglesTienenLasMismasClaves() {

		assertEquals(claves("es"), claves("en"));
	}

	@Test
	void cambiarDeIdiomaCambiaLosTextos() {

		Mensajes.setIdioma("es");
		String es = Mensajes.get("login.continuar");
		Mensajes.setIdioma("en");

		assertNotEquals(es, Mensajes.get("login.continuar"));
		assertEquals("English", Mensajes.nombreDeIdioma("en"));
		assertEquals("Español", Mensajes.nombreDeIdioma("es"));
	}

	@Test
	void unIdiomaInexistenteSeRechaza() {

		assertThrows(IllegalArgumentException.class, () -> Mensajes.setIdioma("xx"));
		assertThrows(IllegalArgumentException.class, () -> Mensajes.setIdioma(null));
	}

	@Test
	void lasPlantillasAceptanArgumentos() {

		Mensajes.setIdioma("en");
		assertEquals("Hello, Ana (a@x.com)", Mensajes.get("login.hola", "Ana", "a@x.com"));
	}

	@Test
	void todasLasClavesQueUsaElCodigoExisten() throws IOException {

		Set<String> existentes = claves("es");
		Set<String> usadas = new TreeSet<>();
		Pattern enJava = Pattern.compile("Mensajes\\.get\\(\"([^\"]+)\"\\s*[,)]");
		Pattern enFxml = Pattern.compile("=\"%([\\w.]+)\"");

		try (Stream<Path> archivos = Files.walk(Path.of("src"))) {

			for (Path p : archivos.filter(Files::isRegularFile).toList()) {

				String nombre = p.toString();
				Pattern patron = nombre.endsWith(".java") ? enJava : nombre.endsWith(".fxml") ? enFxml : null;

				if (patron == null) {

					continue;
				}

				Matcher m = patron.matcher(Files.readString(p));

				while (m.find()) {

					usadas.add(m.group(1));
				}
			}
		}

		// claves que se arman en tiempo de ejecucion
		for (EstadoViaje e : EstadoViaje.values()) {

			usadas.add("estado." + e.name());
			usadas.add("estado.desc." + e.name());
		}

		for (viajes.CalificacionViaje c : viajes.CalificacionViaje.values()) {

			usadas.add("calificacion." + c.name());
		}

		for (String k : new String[] { "origen", "destino", "conductor", "elegirOrigen", "elegirDestino",
				"tilesFallidas", "fondoActualizado", "cargando", "reintentar", "esperar" }) {

			usadas.add("mapa." + k);
		}

		Set<String> faltan = new TreeSet<>(usadas);
		faltan.removeAll(existentes);

		assertTrue(usadas.size() > 50, "no se encontraron las claves usadas");
		assertTrue(faltan.isEmpty(), "claves usadas que no existen: " + faltan);
	}

	@Test
	void elIdiomaBaseEsEspanolAunqueElSistemaEsteEnOtroIdioma() {

		Locale anterior = Locale.getDefault();

		try {

			Locale.setDefault(Locale.forLanguageTag("en"));
			Mensajes.setIdioma("es");
			assertEquals("Continuar", Mensajes.get("login.continuar"));

		} finally {

			Locale.setDefault(anterior);
		}
	}
}
