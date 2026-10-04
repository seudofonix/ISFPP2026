/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 */
package servicio;

import java.util.Objects;

import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;

public class Servicio {

    private String nombre;
    private double tarifaBase;
    private double precioKm;
    private double precioMinuto;
    private TipoVehiculo tipoVehiculo;
    private CategoriaVehiculo categoriaVehiculo;
    private TipoServicio tipoServicio;

    /**
     * Constructor completo para inicializar las tarifas y configuraciones del servicio.
     */
    public Servicio(String nombre, double tarifaBase, double precioKm, double precioMinuto,
                    TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo, TipoServicio tipoServicio) {
        this.nombre = nombre;
        this.tarifaBase = tarifaBase;
        this.precioKm = precioKm;
        this.precioMinuto = precioMinuto;
        this.tipoVehiculo = tipoVehiculo;
        this.categoriaVehiculo = categoriaVehiculo;
        this.tipoServicio = tipoServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(double tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    public double getPrecioKm() {
        return precioKm;
    }

    public void setPrecioKm(double precioKm) {
        this.precioKm = precioKm;
    }

    public double getPrecioMinuto() {
        return precioMinuto;
    }

    public void setPrecioMinuto(double precioMinuto) {
        this.precioMinuto = precioMinuto;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public CategoriaVehiculo getCategoriaVehiculo() {
        return categoriaVehiculo;
    }

    public void setCategoriaVehiculo(CategoriaVehiculo categoriaVehiculo) {
        this.categoriaVehiculo = categoriaVehiculo;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public double calcularCosto(double distanciaKm, double duracionMinutos) {
        return tarifaBase + (distanciaKm * precioKm) + (duracionMinutos * precioMinuto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servicio servicio = (Servicio) o;
        return Objects.equals(nombre, servicio.nombre) &&
               tipoVehiculo == servicio.tipoVehiculo &&
               categoriaVehiculo == servicio.categoriaVehiculo &&
               tipoServicio == servicio.tipoServicio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, tipoVehiculo, categoriaVehiculo, tipoServicio);
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "nombre='" + nombre + '\'' +
                ", tarifaBase=" + tarifaBase +
                ", precioKm=" + precioKm +
                ", precioMinuto=" + precioMinuto +
                ", tipoVehiculo=" + tipoVehiculo +
                ", categoriaVehiculo=" + categoriaVehiculo +
                ", tipoServicio=" + tipoServicio +
                '}';
    }
}
