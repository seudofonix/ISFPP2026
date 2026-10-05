/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Enumera las calificaciones que pueden asignarse a los participantes de un viaje.
 */
package viajes;

public enum CalificacionViaje {
	NO_CALIFICADO(0),
	MALO(1),
	REGULAR(2),
	BUENO(3),
	MUY_BUENO(4),
	EXCELENTE(5);

	private final int valor;

	CalificacionViaje(int valor) {
		this.valor = valor;
	}

	public int getValor() {
		return valor;
	}
}
