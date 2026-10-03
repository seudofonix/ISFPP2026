/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define al conductor autorizado para realizar viajes con un vehículo.
 */
package usuario;

import servicio.*;
import ubicacion.*;
import vehiculo.*;
import viajes.*;

import java.util.*;

public class Conductor {

    private String licenciaConducir;
    private CategoriaVehiculo categoriaVehiculoActivo;
    private EstadoConductor estadoConductor;
    private List<Viaje> viajes;
    private Map<String, Vehiculo> vehiculos;

    /**
     * Crea al conductor e inicializa su registro de viajes y vehiculos.
     *
     * @param licenciaConducir licencia unica para cada conductor.
     */
    public Conductor(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
        this.estadoConductor = EstadoConductor.FUERA_DE_SERVICIO;
        this.categoriaVehiculoActivo = null;
        this.viajes = new ArrayList<>();
        this.vehiculos = new HashMap<>();
    }

    public String getLicenciaConducir() {
        return licenciaConducir;
    }

    public void setLicenciaConducir(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
    }

    public CategoriaVehiculo getCategoriaVehiculoActivo() {
        return categoriaVehiculoActivo;
    }

    public void setCategoriaVehiculoActivo(CategoriaVehiculo categoriaVehiculoActivo) {
        this.categoriaVehiculoActivo = categoriaVehiculoActivo;
    }

    public EstadoConductor getEstadoConductor() {
        return estadoConductor;
    }

    public void setEstadoConductor(EstadoConductor estadoConductor) {
        this.estadoConductor = estadoConductor;
    }

    public List<Viaje> getViajes() {
        return new ArrayList<>(viajes);
    }

    public Map<String, Vehiculo> getVehiculos() {
        return new HashMap<>(vehiculos);
    }

    /**
     * Agrega un nuevo vehiculo del conductor a su registro, usando la patente del mismo para identificarlo
     *
     * @param vehiculo nuevo vehiculo perteneciente al conductor
     */

    public void agregarVehiculo (Vehiculo vehiculo) {
        vehiculos.put(vehiculo.getPatente(), vehiculo);
    }

    /**
     * Agrega un viaje al registro de viajes del conductor.
     *
     * @param viaje ultimo viaje realizado por el conductor a registrar
     */
    public void agregarViaje (Viaje viaje) {
        viajes.add(viaje);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Conductor conductor = (Conductor) o;
        return Objects.equals(licenciaConducir, conductor.licenciaConducir);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(licenciaConducir);
    }

    @Override
    public String toString() {
        return "Conductor{" +
                "licenciaConducir='" + licenciaConducir + '\'' +
                ", categoriaVehiculoActivo=" + categoriaVehiculoActivo +
                ", estadoConductor=" + estadoConductor +
                ", viajes=" + viajes +
                ", vehiculos=" + vehiculos +
                '}';
    }
}
