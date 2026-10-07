package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import servicio.Servicio;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
import viajes.EstadoViaje;
import viajes.Viaje;

class ViajeTest {

    private Usuario cliente;
    private Usuario conductor;
    private Vehiculo vehiculo;
    private Servicio servicio;
    private Ubicacion origen;
    private Ubicacion destino;
    private Viaje viaje;

    @BeforeEach
    void setUp() {
        // 1. Configurar Entidades Básicas
        origen = new Ubicacion(-42.76, -65.03);
        destino = new Ubicacion(-42.78, -65.05);

        // Servicio estándar de pasajeros en auto
        servicio = new Servicio("Estándar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        // 2. Configurar Cliente
        cliente = new Usuario("Gastón Silva", "+5491122223333", "gaston@email.com");
        // Aseguramos que empiece como CLIENTE activo
        cliente.cambiarRolActivo(RolUsuario.CLIENTE);

        // 3. Configurar Conductor y su Vehículo compatible
        vehiculo = new Vehiculo("AA123BB", "Toyota Corolla", 4,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        conductor = new Usuario("Carlos Gómez", "+5491144445555", "carlos@email.com");
        conductor.altaConductor("LIC-12345", vehiculo);
        // Ponemos al chofer en estado operativo para la simulación de aceptación
        conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);
        conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);

        // 4. Instanciar la unidad bajo prueba (SUT)
        viaje = new Viaje(cliente, origen, destino, servicio);
    }

    @Test
    @DisplayName("1. Probar flujo exitoso de Solicitar Viaje")
    void testSolicitarExitoso() {
        LocalDateTime ahora = LocalDateTime.now();

        assertNull(viaje.estadoActual(), "El viaje no debería tener estado inicial antes de solicitarse.");

        viaje.solicitar(ahora);

        assertEquals(EstadoViaje.SOLICITADO, viaje.estadoActual());
        assertEquals(1, viaje.getRegistroViaje().size(), "Debería haber exactamente un hito en el historial.");
        assertTrue(cliente.getCliente().enViaje(), "El cliente debería figurar en un viaje activo.");
    }

    @Test
    @DisplayName("1b. Probar error al solicitar viaje si el cliente ya está en otro viaje activo")
    void testSolicitarErrorClienteEnViaje() {
        viaje.solicitar(LocalDateTime.now()); // Primer viaje activo

        // Intentar crear y solicitar un segundo viaje simultáneo
        Viaje segundoViaje = new Viaje(cliente, origen, destino, servicio);

        assertThrows(IllegalStateException.class, () -> {
            segundoViaje.solicitar(LocalDateTime.now());
        }, "Debería fallar porque el cliente ya se encuentra en viaje.");
    }

    @Test
    @DisplayName("2. Probar flujo exitoso de Aceptar Viaje")
    void testAceptarExitoso() {
        viaje.solicitar(LocalDateTime.now());

        viaje.aceptar(LocalDateTime.now(), conductor);

        assertEquals(EstadoViaje.ACEPTADO, viaje.estadoActual());
        assertEquals(conductor, viaje.getConductor(), "El conductor asignado debe coincidir.");
        assertEquals(vehiculo, viaje.getVehiculo(), "El vehículo debe quedar vinculado al viaje.");
        assertEquals(EstadoConductor.VIAJE_A_ORIGEN, conductor.getConductor().getEstadoConductor(),
                "El chofer debería cambiar su estado a VIAJE_A_ORIGEN.");
    }

    @Test
    @DisplayName("2b. Probar error al aceptar si el vehículo del chofer es de categoría inferior")
    void testAceptarErrorCategoriaInferior() {
        viaje.solicitar(LocalDateTime.now());

        // Modificamos el requerimiento del servicio a PREMIUM
        servicio.setCategoriaVehiculo(CategoriaVehiculo.PREMIUM);
        // El vehículo del conductor configurado en el setUp es ESTANDAR

        assertThrows(IllegalStateException.class, () -> {
            viaje.aceptar(LocalDateTime.now(), conductor);
        }, "Debería rechazar al conductor porque su vehículo no cumple con el nivel de confort exigido.");
    }

    @Test
    @DisplayName("3. Probar flujo exitoso de Iniciar Viaje")
    void testIniciarExitoso() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), conductor);

        viaje.iniciar(LocalDateTime.now());

        assertEquals(EstadoViaje.INICIADO, viaje.estadoActual());
        assertEquals(EstadoConductor.VIAJE_A_DESTINO, conductor.getConductor().getEstadoConductor(),
                "El chofer debería cambiar su estado a VIAJE_A_DESTINO.");
    }

    @Test
    @DisplayName("3b. Probar error al iniciar viaje si este no fue aceptado previamente")
    void testIniciarErrorSinAceptar() {
        viaje.solicitar(LocalDateTime.now());
        // Nos saltamos el paso de aceptar()

        assertThrows(IllegalStateException.class, () -> {
            viaje.iniciar(LocalDateTime.now());
        }, "No se puede iniciar un viaje que está únicamente en estado SOLICITADO.");
    }

    @Test
    @DisplayName("4. Probar flujo exitoso de Finalizar Viaje")
    void testFinalizarExitoso() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), conductor);
        viaje.iniciar(LocalDateTime.now());

        viaje.finalizar(LocalDateTime.now(), CalificacionViaje.EXCELENTE, CalificacionViaje.BUENO);

        assertEquals(EstadoViaje.FINALIZADO, viaje.estadoActual());
        assertEquals(CalificacionViaje.EXCELENTE, viaje.getCalificacionCliente());
        assertEquals(CalificacionViaje.BUENO, viaje.getCalificacionConductor());
        assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor(),
                "El conductor debe quedar DISPONIBLE para nuevos viajes tras finalizar.");
    }

    @Test
    @DisplayName("5. Probar flujo exitoso de Cancelar Viaje por parte del Cliente")
    void testCancelarExitosoPorCliente() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), conductor);

        viaje.cancelar(LocalDateTime.now(), cliente, "El plan cambió");

        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
        assertEquals("El plan cambió", viaje.getMotivoCancelacion());
        assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor(),
                "El conductor debe volver a estar DISPONIBLE tras la cancelación.");
    }

    @Test
    @DisplayName("6. Probar flujo exitoso de Rechazar Viaje")
    void testRechazarExitoso() {
        viaje.solicitar(LocalDateTime.now());

        viaje.rechazar(LocalDateTime.now());

        assertEquals(EstadoViaje.RECHAZADO, viaje.estadoActual());
    }

    @Test
    @DisplayName("6b. Probar error al rechazar un viaje que ya se inició")
    void testRechazarErrorViajeIniciado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), conductor);
        viaje.iniciar(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> {
            viaje.rechazar(LocalDateTime.now());
        }, "No se puede rechazar un viaje que ya se encuentra en curso (INICIADO).");
    }
}
