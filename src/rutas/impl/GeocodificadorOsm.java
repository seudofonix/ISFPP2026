/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Geocodificador inverso local. El nombre de la calle lo informa el motor de rutas (la calle del grafo mas
 * cercana al punto) y la altura se estima con los domicilios (addr:street + addr:housenumber) del mismo
 * extracto OpenStreetMap que usa el motor: si hay un domicilio a menos de 25 m se usa su numero, y si no se
 * interpola entre los dos mas cercanos de esa calle ("al 115"). El archivo se lee en segundo plano al crear el
 * objeto, asi la primera consulta no demora el arranque de la aplicacion.
 */
package rutas.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import rutas.api.Geocodificador;
import rutas.api.PlanificadorRutas;
import ubicacion.Ubicacion;

public final class GeocodificadorOsm implements Geocodificador {

	private static final double METROS_POR_GRADO = 111_320.0;
	private static final double DISTANCIA_EXACTA_M = 25.0;
	private static final double DISTANCIA_MAXIMA_M = 400.0;
	private static final double DISTANCIA_SIN_CALLE_M = 80.0;
	private static final String SIN_NOMBRE = "Calle sin nombre";

	private final PlanificadorRutas motor;
	private final CompletableFuture<Map<String, List<Domicilio>>> domicilios;

	private record Domicilio(double latitud, double longitud, int numero, String calle) {
	}

	/**
	 * @param motor      motor de rutas que informa el nombre de la calle de un punto
	 * @param archivoOsm extracto OSM con los domicilios; si es null solo se informa la calle
	 */
	public GeocodificadorOsm(PlanificadorRutas motor, String archivoOsm) {

		this.motor = Objects.requireNonNull(motor, "motor");

		if (archivoOsm == null) {

			this.domicilios = CompletableFuture.completedFuture(Map.of());

		} else {

			this.domicilios = CompletableFuture.supplyAsync(() -> leer(Path.of(archivoOsm)))
					.exceptionally(error -> {

						System.err.println("No se pudieron leer los domicilios de " + archivoOsm + ": "
								+ error.getMessage() + ". Se informara solo el nombre de la calle.");
						return Map.of();
					});
		}
	}

	@Override
	public String direccion(Ubicacion ubicacion) {

		if (ubicacion == null) {

			return SIN_NOMBRE;
		}

		Map<String, List<Domicilio>> porCalle = domicilios.join();
		Optional<String> calle = motor.nombreCalle(ubicacion);

		if (calle.isPresent()) {

			List<Domicilio> lista = porCalle.get(normalizar(calle.get()));

			return lista == null ? calle.get() + " s/n" : conAltura(calle.get(), lista, ubicacion);
		}

		// la calle del grafo no tiene nombre: se usa el domicilio mas cercano si esta muy cerca
		Domicilio cercano = null;
		double mejor = DISTANCIA_SIN_CALLE_M;

		for (List<Domicilio> lista : porCalle.values()) {

			for (Domicilio d : lista) {

				double distancia = metros(ubicacion, d);

				if (distancia <= mejor) {

					mejor = distancia;
					cercano = d;
				}
			}
		}

		return cercano == null ? SIN_NOMBRE : conAltura(cercano.calle(), porCalle.get(normalizar(cercano.calle())),
				ubicacion);
	}

	/** Estima la altura con los dos domicilios mas cercanos de la calle (ponderando por distancia inversa). */
	private String conAltura(String calle, List<Domicilio> lista, Ubicacion punto) {

		Domicilio primero = null;
		Domicilio segundo = null;
		double d1 = Double.MAX_VALUE;
		double d2 = Double.MAX_VALUE;

		for (Domicilio d : lista) {

			double distancia = metros(punto, d);

			if (distancia < d1) {

				segundo = primero;
				d2 = d1;
				primero = d;
				d1 = distancia;

			} else if (distancia < d2) {

				segundo = d;
				d2 = distancia;
			}
		}

		if (primero == null || d1 > DISTANCIA_MAXIMA_M) {

			return calle + " s/n";
		}

		if (d1 <= DISTANCIA_EXACTA_M) {

			return calle + " " + primero.numero();
		}

		int numero = primero.numero();

		if (segundo != null && d2 <= DISTANCIA_MAXIMA_M) {

			numero = (int) Math.round((primero.numero() * d2 + segundo.numero() * d1) / (d1 + d2));
		}

		return calle + " al " + numero;
	}

	private static double metros(Ubicacion punto, Domicilio d) {

		double dLat = (punto.getLatitud() - d.latitud()) * METROS_POR_GRADO;
		double dLon = (punto.getLongitud() - d.longitud()) * METROS_POR_GRADO
				* Math.cos(Math.toRadians(punto.getLatitud()));

		return Math.hypot(dLat, dLon);
	}

	private static String normalizar(String texto) {

		String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

		return sinAcentos.trim().toLowerCase().replaceAll("\\s+", " ");
	}

	/** Lee del .osm los nodos que tienen calle y numero de puerta (lectura en streaming, sin cargar el XML entero). */
	private static Map<String, List<Domicilio>> leer(Path archivo) {

		Map<String, List<Domicilio>> resultado = new HashMap<>();
		XMLInputFactory fabrica = XMLInputFactory.newInstance();
		fabrica.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		fabrica.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

		try (InputStream entrada = new BufferedInputStream(Files.newInputStream(archivo))) {

			XMLStreamReader lector = fabrica.createXMLStreamReader(entrada);
			boolean enNodo = false;
			double latitud = 0;
			double longitud = 0;
			String calle = null;
			String numero = null;

			try {

				while (lector.hasNext()) {

					int evento = lector.next();

					if (evento == XMLStreamConstants.START_ELEMENT) {

						String nombre = lector.getLocalName();

						if (nombre.equals("node")) {

							enNodo = true;
							calle = null;
							numero = null;
							latitud = numeroDe(lector.getAttributeValue(null, "lat"));
							longitud = numeroDe(lector.getAttributeValue(null, "lon"));

						} else if (nombre.equals("tag") && enNodo) {

							String clave = lector.getAttributeValue(null, "k");

							if ("addr:street".equals(clave)) {

								calle = lector.getAttributeValue(null, "v");

							} else if ("addr:housenumber".equals(clave)) {

								numero = lector.getAttributeValue(null, "v");
							}

						} else if (!nombre.equals("tag")) {

							enNodo = false;
						}

					} else if (evento == XMLStreamConstants.END_ELEMENT && lector.getLocalName().equals("node")) {

						agregar(resultado, calle, numero, latitud, longitud);
						enNodo = false;
					}
				}

			} finally {

				lector.close();
			}

		} catch (IOException | XMLStreamException e) {

			throw new IllegalStateException(e.getMessage(), e);
		}

		return resultado;
	}

	private static void agregar(Map<String, List<Domicilio>> destino, String calle, String numero, double latitud,
			double longitud) {

		if (calle == null || numero == null || Double.isNaN(latitud) || Double.isNaN(longitud)) {

			return;
		}

		int digitos = 0;

		while (digitos < numero.length() && Character.isDigit(numero.charAt(digitos))) {

			digitos++;
		}

		if (digitos == 0 || digitos > 6) {

			return; // "s/n" u otros numeros no numericos
		}

		int valor = Integer.parseInt(numero.substring(0, digitos));
		destino.computeIfAbsent(normalizar(calle), k -> new ArrayList<>())
				.add(new Domicilio(latitud, longitud, valor, calle.trim()));
	}

	private static double numeroDe(String texto) {

		try {

			return Double.parseDouble(texto);

		} catch (NumberFormatException | NullPointerException e) {

			return Double.NaN;
		}
	}
}
