/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Textos de la aplicacion en el idioma elegido. Los mensajes viven en mensajes.properties (espanol, idioma
 * base) y mensajes_en.properties (ingles). El idioma inicial se configura con la clave 'idioma' de
 * config.properties y las pantallas lo pueden cambiar en tiempo de ejecucion. Para agregar otro idioma basta
 * con crear mensajes_XX.properties y sumar el codigo a IDIOMAS.
 */
package idioma;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Mensajes {

	/** Codigos de idioma disponibles; el primero es el idioma base. */
	public static final List<String> IDIOMAS = List.of("es", "en");

	private static final String BASE = "idioma.mensajes";

	private static volatile Locale idioma = Locale.forLanguageTag(IDIOMAS.get(0));
	private static volatile ResourceBundle textos = cargar(idioma);

	private Mensajes() {
	}

	/**
	 * Cambia el idioma de toda la aplicacion.
	 *
	 * @param codigo "es" o "en" (no distingue mayusculas)
	 * @throws IllegalArgumentException si el idioma no esta disponible
	 */
	public static void setIdioma(String codigo) {

		String limpio = codigo == null ? "" : codigo.trim().toLowerCase(Locale.ROOT);

		if (!IDIOMAS.contains(limpio)) {

			throw new IllegalArgumentException("Idioma no disponible: '" + codigo + "'. Opciones: " + IDIOMAS);
		}

		Locale nuevo = Locale.forLanguageTag(limpio);
		textos = cargar(nuevo);
		idioma = nuevo;
	}

	public static Locale getIdioma() {

		return idioma;
	}

	/** Nombre del idioma escrito en ese mismo idioma (para mostrarlo en un selector). */
	public static String nombreDeIdioma(String codigo) {

		return cargar(Locale.forLanguageTag(codigo)).getString("idioma.nombre");
	}

	/** Bundle del idioma actual (lo usa el FXMLLoader para resolver los textos %clave de los FXML). */
	public static ResourceBundle getBundle() {

		return textos;
	}

	/** Texto de una clave en el idioma actual. */
	public static String get(String clave) {

		return textos.getString(clave);
	}

	/** Texto con argumentos (formato String.format: %s, %d, %.2f) en el idioma actual. */
	public static String get(String clave, Object... argumentos) {

		return String.format(idioma, textos.getString(clave), argumentos);
	}

	private static ResourceBundle cargar(Locale locale) {

		return ResourceBundle.getBundle(BASE, locale, ResourceBundle.Control.getNoFallbackControl(
				ResourceBundle.Control.FORMAT_PROPERTIES));
	}
}
