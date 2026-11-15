/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Permite que GestorViajes pregunte cuantos segundos debe durar cada tramo simulado de un viaje (por ejemplo,
 * proporcional a la distancia). Es opcional: sin calculador, todos los tramos duran ConfiguracionSimulacion.segundosTramo.
 */
package modelo;

import viajes.EstadoViaje;
import viajes.Viaje;

@FunctionalInterface
public interface CalculadorDuracionTramo {

	/**
	 * @param viaje viaje en curso (ya tiene conductor y vehiculo asignados)
	 * @param tramo ACEPTADO = el conductor va hacia el origen; INICIADO = va del origen al destino
	 * @return segundos simulados que dura ese tramo (mayor que 0)
	 */
	double segundos(Viaje viaje, EstadoViaje tramo);
}
