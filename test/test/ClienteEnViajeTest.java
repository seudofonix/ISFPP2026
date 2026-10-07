package test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import servicio.Servicio;
import servicio.TipoServicio;
import ubicacion.Ubicacion;
import usuario.Cliente;
import usuario.EstadoConductor;
import usuario.RolUsuario;
import usuario.Usuario;
import vehiculo.CategoriaVehiculo;
import vehiculo.TipoVehiculo;
import vehiculo.Vehiculo;
import viajes.CalificacionViaje;
import viajes.Viaje;

class ClienteEnViajeTest {

    private Usuario usuarioCliente;
    private Usuario usuarioConductor;
    private Viaje viaje;
    private Servicio servicio;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        // Inicializamos el entorno básico
        ubicacion = new Ubicacion(-42.76, -65.03);
        servicio = new Servicio("Estándar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        usuarioCliente = new Usuario("Sofía Alvarez", "+549113170528", "sofia.alvarez@email.com");
        usuarioCliente.cambiarRolActivo(RolUsuario.CLIENTE);

        Vehiculo vehiculo = new Vehiculo("AB456DE", "Ford Focus", 4,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        usuarioConductor = new Usuario("Ana Martinez", "+549118220576", "ana.martinez@email.com");
        usuarioConductor.altaConductor("B1-D1", vehiculo);
        usuarioConductor.cambiarRolActivo(RolUsuario.CONDUCTOR);
        usuarioConductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);

        viaje = new Viaje(usuarioCliente, ubicacion, ubicacion, servicio);
    }

    @Test
    @DisplayName("1. Un cliente nuevo sin historial de viajes NO debe estar en viaje")
    void testClienteSinViajes() {
        Cliente cliente = usuarioCliente.getCliente();
        assertFalse(cliente.enViaje(), "Un cliente sin viajes creados debería retornar false.");
    }

    @Test
    @DisplayName("2. El cliente SI está en viaje cuando el estado es SOLICITADO")
    void testEnViajeEstadoSolicitado() {
        viaje.solicitar(LocalDateTime.now());

        assertTrue(usuarioCliente.getCliente().enViaje(),
                "Debería retornar true si el último viaje fue SOLICITADO.");
    }

    @Test
    @DisplayName("3. El cliente SI está en viaje cuando el estado es ACEPTADO")
    void testEnViajeEstadoAceptado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), usuarioConductor);

        assertTrue(usuarioCliente.getCliente().enViaje(),
                "Debería retornar true si el último viaje fue ACEPTADO.");
    }

    @Test
    @DisplayName("4. El cliente SI está en viaje cuando el estado es INICIADO")
    void testEnViajeEstadoIniciado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), usuarioConductor);
        viaje.iniciar(LocalDateTime.now());

        assertTrue(usuarioCliente.getCliente().enViaje(),
                "Debería retornar true si el último viaje fue INICIADO (en curso).");
    }

    @Test
    @DisplayName("5. El cliente NO está en viaje cuando el estado es FINALIZADO")
    void testNoEnViajeEstadoFinalizado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), usuarioConductor);
        viaje.iniciar(LocalDateTime.now());
        viaje.finalizar(LocalDateTime.now(), CalificacionViaje.EXCELENTE, CalificacionViaje.EXCELENTE);

        assertFalse(usuarioCliente.getCliente().enViaje(),
                "Debería retornar false una vez que el viaje fue FINALIZADO.");
    }

    @Test
    @DisplayName("6. El cliente NO está en viaje cuando el estado es CANCELADO")
    void testNoEnViajeEstadoCancelado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.cancelar(LocalDateTime.now(), usuarioCliente, "Me arrepentí");

        assertFalse(usuarioCliente.getCliente().enViaje(),
                "Debería retornar false si el viaje fue CANCELADO.");
    }

    @Test
    @DisplayName("7. El cliente NO está en viaje cuando el estado es RECHAZADO")
    void testNoEnViajeEstadoRechazado() {
        viaje.solicitar(LocalDateTime.now());
        viaje.rechazar(LocalDateTime.now());

        assertFalse(usuarioCliente.getCliente().enViaje(),
                "Debería retornar false si el viaje fue RECHAZADO.");
    }
}