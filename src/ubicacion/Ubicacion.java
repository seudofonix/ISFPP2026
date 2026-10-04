/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 */
package ubicacion;

import java.util.Objects;

public class Ubicacion {

    private double latitud;
    private double longitud;

    public Ubicacion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    /**
     * Calcula la distancia estimada en kilómetros hacia otra ubicación usando la fórmula de Haversine.
     */
    public double calcularDistancia(Ubicacion otraUbicacion) {
        if (otraUbicacion == null) return 0.0;

        final int RADIO_TIERRA_KM = 6371;

        double dLat = Math.toRadians(otraUbicacion.getLatitud() - this.latitud);
        double dLon = Math.toRadians(otraUbicacion.getLongitud() - this.longitud);

        double lat1 = Math.toRadians(this.latitud);
        double lat2 = Math.toRadians(otraUbicacion.getLatitud());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return Double.compare(ubicacion.latitud, latitud) == 0 &&
               Double.compare(ubicacion.longitud, longitud) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }

    @Override
    public String toString() {
        return "Ubicacion{" +
                "latitud=" + latitud +
                ", longitud=" + longitud +
                '}';
    }
}
