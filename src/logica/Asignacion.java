/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Conductor elegido para un servicio junto con el vehiculo con el que lo prestaria.
 */
package logica;

import usuario.Usuario;
import vehiculo.Vehiculo;

public record Asignacion(Usuario conductor, Vehiculo vehiculo, double distanciaKm) {
}
