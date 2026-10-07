package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import viajes.Viaje;

class UsuarioRolTest {

    private Usuario usuario;
    private Vehiculo vehiculo;
    private Servicio servicio;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        // Inicializamos un usuario base (por defecto arranca como CLIENTE activo)
        usuario = new Usuario("Lucas Benitez", "+5491133334444", "lucas@email.com");

        // Elementos necesarios para simular viajes o registrar conductores
        vehiculo = new Vehiculo("CH123XY", "Ford Focus", 4,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        servicio = new Servicio("Estándar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        ubicacion = new Ubicacion(-42.76, -65.03);
    }

    @Test
    @DisplayName("1. Un usuario nuevo debe iniciar siempre con el rol CLIENTE")
    void testRolInicialEsCliente() {
        assertEquals(RolUsuario.CLIENTE, usuario.getRolActivo(),
                "El rol por defecto al crear un usuario debe ser CLIENTE.");
    }

    @Test
    @DisplayName("2. Cambiar exitosamente de CLIENTE a CONDUCTOR")
    void testCambioAConductorExitoso() {
        // Damos de alta como conductor primero para que tenga la entidad asociada
        usuario.altaConductor("LIC-9999", vehiculo);

        // El estado inicial del conductor tras el alta es FUERA_DE_SERVICIO
        assertEquals(EstadoConductor.FUERA_DE_SERVICIO, usuario.getConductor().getEstadoConductor());

        // Ejecutamos el cambio de rol
        usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);

        assertEquals(RolUsuario.CONDUCTOR, usuario.getRolActivo(),
                "El rol activo debería haber mutado a CONDUCTOR.");
    }

    @Test
    @DisplayName("3. Error al cambiar a CONDUCTOR si el usuario no está dado de alta como tal")
    void testCambioAConductorSinAltaError() {
        // Intentamos cambiar sin haber llamado a usuario.altaConductor(...)
        assertThrows(IllegalStateException.class, () -> {
            usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        }, "Debería fallar porque el objeto Conductor interno es null.");
    }

    @Test
    @DisplayName("4. Error al cambiar a CONDUCTOR si ya se encuentra en un estado operativo (ej. DISPONIBLE)")
    void testCambioAConductorSiYaEstaOperativoError() {
        usuario.altaConductor("LIC-9999", vehiculo);

        // Forzamos un estado operativo simulando una acción del sistema
        usuario.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);

        assertThrows(IllegalStateException.class, () -> {
            usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        }, "Debería fallar porque sólo permite cambiar si el estado actual es FUERA_DE_SERVICIO.");
    }

    @Test
    @DisplayName("5. Cambiar exitosamente de CONDUCTOR a CLIENTE pone al chofer en FUERA_DE_SERVICIO")
    void testCambioAClienteExitoso() {
        usuario.altaConductor("LIC-9999", vehiculo);

        // Primero pasamos a Conductor
        usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        // El sistema lo pone DISPONIBLE para trabajar
        usuario.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);

        // El conductor decide volver a ser Cliente (apagar la app de conductor)
        usuario.cambiarRolActivo(RolUsuario.CLIENTE);

        assertEquals(RolUsuario.CLIENTE, usuario.getRolActivo());
        assertEquals(EstadoConductor.FUERA_DE_SERVICIO, usuario.getConductor().getEstadoConductor(),
                "Al bajarse del rol de conductor, su estado operativo debe pasar automáticamente a FUERA_DE_SERVICIO.");
    }

    @Test
    @DisplayName("6. Error al cambiar de rol si el usuario tiene un viaje activo como CLIENTE")
    void testCambioRolConViajeClienteActivoError() {
        // Crear un viaje y simular que se solicita para que quede activo
        Viaje viaje = new Viaje(usuario, ubicacion, ubicacion, servicio);
        viaje.solicitar(LocalDateTime.now());

        // También le damos el alta de conductor para cumplir esa restricción
        usuario.altaConductor("LIC-9999", vehiculo);

        // Intentamos quitarle el rol de cliente mientras viaja
        assertThrows(IllegalStateException.class, () -> {
            usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        }, "Debería rechazar el cambio porque el cliente posee un viaje en curso (SOLICITADO/ACEPTADO/INICIADO).");
    }
}
