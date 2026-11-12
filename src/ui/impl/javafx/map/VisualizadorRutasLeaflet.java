/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Implementacion de VisualizadorMapa con WebView y Leaflet. Carga /ui/map/mapa.html con el WebEngine, espera a
 * que termine de cargar, publica el puente "javaBridge" y recien entonces ejecuta las funciones JavaScript
 * pendientes (window.mostrarRutas, window.activarSeleccion...). Le entrega a JavaScript solo estructuras de
 * datos; los colores, el zoom y la sintaxis de Leaflet quedan en mapa.html.
 */
package ui.impl.javafx.map;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import datos.CargarParametros;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import rutas.api.PuntoRuta;
import rutas.api.RutaCalculada;
import rutas.api.RutasViaje;
import ubicacion.Ubicacion;
import ui.api.ReceptorPuntoMapa;
import ui.api.VisualizadorMapa;

public final class VisualizadorRutasLeaflet implements VisualizadorMapa {

	private static final String PAGINA = "/ui/map/mapa.html";
	private static final String USER_AGENT = "MovilidadDigital-ISFPP2026/1.0 (practica academica; JavaFX WebView)";

	private final WebView webView;
	private final WebEngine engine;
	private final List<String> scriptsPendientes = new ArrayList<>();

	private PuenteSeleccionMapa puente; // referencia fuerte: si se pierde, JavaScript deja de poder llamarlo
	private boolean paginaLista;

	public VisualizadorRutasLeaflet(WebView webView) {

		this.webView = Objects.requireNonNull(webView, "webView");
		this.engine = webView.getEngine();

		engine.setUserAgent(USER_AGENT);

		engine.getLoadWorker().stateProperty().addListener((obs, anterior, estado) -> {

			if (estado == Worker.State.SUCCEEDED) {

				paginaLista = true;
				conectarPuente();
				ejecutarDirecto(String.format(Locale.ROOT, "window.configurarVistaInicial(%f, %f, %d);",
						CargarParametros.getMapaCentroLatitud(), CargarParametros.getMapaCentroLongitud(),
						CargarParametros.getMapaZoom()));
				vaciarPendientes();
				solicitarAjuste();
			}
		});

		webView.widthProperty().addListener((obs, a, b) -> solicitarAjuste());
		webView.heightProperty().addListener((obs, a, b) -> solicitarAjuste());

		URL recurso = getClass().getResource(PAGINA);

		if (recurso == null) {

			throw new IllegalStateException("No se encontro el recurso " + PAGINA);
		}

		engine.load(recurso.toExternalForm());
	}

	@Override
	public void mostrar(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino) {

		ejecutar("window.mostrarRutas(" + aJson(rutas, conductor, origen, destino) + ");");
	}

	@Override
	public void limpiarRutas() {

		ejecutar("window.limpiarRutas();");
	}

	@Override
	public void limpiarSeleccion() {

		ejecutar("window.limpiarSeleccion();");
	}

	@Override
	public void cancelarSeleccion() {

		ejecutar("window.cancelarSeleccion();");
	}

	@Override
	public void configurarSeleccion(ReceptorPuntoMapa receptor) {

		this.puente = new PuenteSeleccionMapa(Objects.requireNonNull(receptor, "receptor"));

		if (paginaLista) {

			conectarPuente();
		}
	}

	@Override
	public void activarSeleccion(String tipo) {

		ejecutar("window.activarSeleccion('" + tipoValido(tipo) + "');");
	}

	@Override
	public void mostrarMarcadorSeleccion(String tipo, Ubicacion ubicacion) {

		ejecutar(String.format(Locale.ROOT, "window.mostrarMarcadorSeleccion('%s', [%f, %f]);", tipoValido(tipo),
				ubicacion.getLatitud(), ubicacion.getLongitud()));
	}

	// ------------------------------------------------------------------ interno

	/** Ejecuta ya si la pagina cargo; si no, queda en espera hasta SUCCEEDED (window.* todavia no existe). */
	private void ejecutar(String script) {

		if (paginaLista) {

			ejecutarDirecto(script);

		} else {

			scriptsPendientes.add(script);
		}
	}

	private void ejecutarDirecto(String script) {

		try {

			engine.executeScript(script);

		} catch (RuntimeException e) {

			System.err.println("Error ejecutando JavaScript del mapa: " + e.getMessage());
		}
	}

	private void vaciarPendientes() {

		List<String> copia = new ArrayList<>(scriptsPendientes);
		scriptsPendientes.clear();

		copia.forEach(this::ejecutarDirecto);
	}

	private void conectarPuente() {

		if (puente == null) {

			return;
		}

		JSObject ventana = (JSObject) engine.executeScript("window");
		ventana.setMember("javaBridge", puente);
	}

	private void solicitarAjuste() {

		Platform.runLater(() -> {

			if (paginaLista) {

				ejecutarDirecto("window.refrescarTamano();");
			}
		});
	}

	private String tipoValido(String tipo) {

		if (!"origen".equals(tipo) && !"destino".equals(tipo)) {

			throw new IllegalArgumentException("Tipo de punto invalido: " + tipo);
		}

		return tipo;
	}

	/**
	 * Estructura que recibe mapa.html: acercamiento y traslado (listas de [lat, lon]) y tres puntos. Leaflet espera
	 * cada coordenada en el orden [latitud, longitud].
	 */
	private String aJson(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino) {

		return "{\"acercamiento\":" + geometria(rutas.haciaOrigen()) + ",\"traslado\":"
				+ geometria(rutas.haciaDestino()) + ",\"conductor\":" + punto(conductor) + ",\"origen\":"
				+ punto(origen) + ",\"destino\":" + punto(destino) + "}";
	}

	private String geometria(RutaCalculada ruta) {

		StringBuilder sb = new StringBuilder("[");

		for (int i = 0; i < ruta.geometria().size(); i++) {

			PuntoRuta p = ruta.geometria().get(i);

			if (i > 0) {

				sb.append(',');
			}

			sb.append(String.format(Locale.ROOT, "[%f,%f]", p.latitud(), p.longitud()));
		}

		return sb.append(']').toString();
	}

	private String punto(Ubicacion u) {

		return String.format(Locale.ROOT, "[%f,%f]", u.getLatitud(), u.getLongitud());
	}
}
