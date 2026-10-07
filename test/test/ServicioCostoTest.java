package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import servicio.Servicio;
import servicio.TipoServicio;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;

class ServicioCostoTest {

    // Delta de tolerancia para comparaciones de números flotantes (double)
    // Evita fallos por imprecisiones de redondeo binario en Java
    private static final double DELTA = 0.001;

    @Test
    @DisplayName("1. Calcular costo para Servicio Estándar Pasajeros")
    void testCalcularCostoEstandar() {
        // Datos basados en servicios.txt: tarifaBase=2000, precioKm=200, precioMinuto=100
        Servicio servicioEstandar = new Servicio("Estandar", 2000.0, 200.0, 100.0,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        // Simulación: Viaje de 10.5 Km que tomó 25.0 minutos
        // Fórmula esperada: 2000 + (10.5 * 200) + (25.0 * 100)
        // Cálculo: 2000 + 2100 + 2500 = 6600.0
        double costoObtenido = servicioEstandar.calcularCosto(10.5, 25.0);

        assertEquals(6600.0, costoObtenido, DELTA,
                "El costo del servicio estándar no coincide con la fórmula polinómica.");
    }

    @Test
    @DisplayName("2. Calcular costo para Servicio Premium Pasajeros")
    void testCalcularCostoPremium() {
        // Datos basados en servicios.txt: tarifaBase=3000, precioKm=300, precioMinuto=150
        Servicio servicioPremium = new Servicio("Premium", 3000.0, 300.0, 150.0,
                TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM, TipoServicio.PASAJEROS);

        // Simulación: Viaje corto y rápido de 3.0 Km en 8.5 minutos
        // Fórmula esperada: 3000 + (3.0 * 300) + (8.5 * 150)
        // Cálculo: 3000 + 900 + 1275 = 5175.0
        double costoObtenido = servicioPremium.calcularCosto(3.0, 8.5);

        assertEquals(5175.0, costoObtenido, DELTA,
                "El costo del servicio premium calculó un valor erróneo.");
    }

    @Test
    @DisplayName("3. Calcular costo para Servicio Mensajería Moto (Valores Bajos)")
    void testCalcularCostoMensajeria() {
        // Datos basados en servicios.txt: tarifaBase=500, precioKm=50, precioMinuto=25
        Servicio servicioMensajeria = new Servicio("Mensajería", 500.0, 50.0, 25.0,
                TipoVehiculo.MOTO, CategoriaVehiculo.ESTANDAR, TipoServicio.ENVIOS);

        // Simulación: Envío a larga distancia de 45.0 Km que demoró 60.0 minutos
        // Fórmula esperada: 500 + (45.0 * 50) + (60.0 * 25)
        // Cálculo: 500 + 2250 + 1500 = 4250.0
        double costoObtenido = servicioMensajeria.calcularCosto(45.0, 60.0);

        assertEquals(4250.0, costoObtenido, DELTA,
                "El costo para el servicio de mensajería en moto es incorrecto.");
    }

    @Test
    @DisplayName("4. Calcular costo con valores en cero (Caso límite)")
    void testCalcularCostoValoresCero() {
        Servicio servicioEstandar = new Servicio("Estandar", 2000.0, 200.0, 100.0,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        // Simulación: Distancia 0 y tiempo 0 (ej. cancelación inmediata si aplicara la base)
        // Fórmula esperada: 2000 + (0 * 200) + (0 * 100) = 2000.0
        double costoObtenido = servicioEstandar.calcularCosto(0.0, 0.0);

        assertEquals(2000.0, costoObtenido, DELTA,
                "Incluso con distancia y tiempo en cero, debería retornar al menos la tarifa base.");
    }
}
