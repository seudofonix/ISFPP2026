/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Traduce los codigos enteros que guarda la base de datos a los enums del dominio: cada enum se
 * guarda como su ordinal (AUTO=0, MOTO=1...) y los tipos de servicio de un vehiculo como mascara de
 * bits (1 = PASAJEROS, 2 = ENVIOS, 3 = ambos).
 */
package datos.factory;

import java.util.HashSet;
import java.util.Set;

import servicio.TipoServicio;

final class CodigosBD {

	private CodigosBD() {
	}

	/** Devuelve la constante del enum cuyo ordinal es el codigo leido. */
	static <E extends Enum<E>> E enumPorCodigo(E[] valores, int codigo, String campo) {

		if (codigo < 0 || codigo >= valores.length) {

			throw new IllegalArgumentException("Codigo invalido " + codigo + " en la columna " + campo);
		}

		return valores[codigo];
	}

	/** Bit de cada TipoServicio dentro de la mascara: 1 << ordinal. */
	static Set<TipoServicio> serviciosDesdeMascara(int mascara) {

		Set<TipoServicio> servicios = new HashSet<>();

		for (TipoServicio tipo : TipoServicio.values()) {

			if ((mascara & (1 << tipo.ordinal())) != 0) {

				servicios.add(tipo);
			}
		}

		return servicios;
	}
}
