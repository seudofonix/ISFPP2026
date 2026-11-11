package md.ui;

import javafx.application.Platform;
import md.logica.PanelClienteController; // O el controlador correspondiente

public class PuenteSeleccionMapa {
    
    private final PanelClienteController controller;

    public PuenteSeleccionMapa(PanelClienteController controller) {
        this.controller = controller;
    }

    /**
     * Método invocado desde JavaScript (Leaflet) al hacer clic en el mapa.
     * Ejemplo en JS: window.javaBridge.seleccionarPunto("destino", lat, lon);
     */
    public void seleccionarPunto(String tipo, double lat, double lon) {
        // Platform.runLater asegura que la actualización de la UI ocurra en el hilo principal de JavaFX
        Platform.runLater(() -> {
            controller.procesarPuntoSeleccionado(tipo, lat, lon);
        });
    }
}