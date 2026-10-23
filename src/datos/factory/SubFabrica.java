/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Sub-factory: cada implementacion sabe crear el lector de un tipo de archivo concreto.
 */
package datos.factory;

public interface SubFabrica<T> {

	Lector<T> crearLector();
}
