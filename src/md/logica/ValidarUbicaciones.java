package md.logica;

import md.rutas.PlanificadorRutas;
import java.util.Optional;

public class ValidacionUbicaciones {

    private final PlanificadorRutas planificadorRutas;
    private final double toleranciaMetros;

    public ValidacionUbicaciones(PlanificadorRutas planificadorRutas, double toleranciaMetros) {
        this.planificadorRutas = planificadorRutas;
        this.toleranciaMetros = toleranciaMetros;
    }

    /**
     * Valida y ajusta una coordenada geográfica cruda a la calle transitable más cercana[cite: 12].
     */
    public Optional<UbicacionValida> validarYAjustar(double lat, double lon) {
        // Verificar si está dentro del área y ajustar a la red vial usando GraphHopper
        return planificadorRutas.ajustarACalle(lat, lon, toleranciaMetros);
    }
}