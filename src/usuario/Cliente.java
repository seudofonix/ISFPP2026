/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define al cliente que solicita y utiliza servicios de transporte.
 */
package usuario;

import servicio.*;
import ubicacion.*;
import vehiculo.*;
import viajes.*;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private List<Viaje> viajes;

    /**
     * Crea al cliente e inicializa su registro de viajes
     */
    public Cliente() {
        viajes = new ArrayList<>();
    }

    public List<Viaje> getViajes() {
        return new ArrayList<>(viajes);
    }

    /**
     * Verifica si el cliente esta o no en un viaje.
     *
     * @return true si es cliente esta actualmente en un viaje o false si este no se encuentra en uno.
     */
    public boolean enViaje() {
        if (viajes.isEmpty()) return false;
        return viajes.getLast().estadoActual() == EstadoViaje.INICIADO;
    }


    /**
     * Agrega un viaje al registro de viajes del cliente
     *
     * @param viaje viaje que realizo el cliente y guardara.
     */
    public void agregarViaje(Viaje viaje) {
        viajes.add(viaje);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "viajes=" + viajes +
                '}';
    }
}
