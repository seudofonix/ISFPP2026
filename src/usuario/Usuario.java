/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Define la entidad base para las personas que interactúan con el sistema.
 */
package usuario;

import servicio.*;
import ubicacion.*;
import vehiculo.*;
import viajes.*;

import java.util.Objects;

public class Usuario {

    private String nombre;
    private String telefono;
    private String email;
    private RolUsuario rolActivo;
    private Cliente cliente;
    private Conductor conductor;

    /**
     * Crea un usuario con su informacion basica y el rol de cliente por defecto.
     *
     * @param nombre nombre del usuario
     * @param telefono numero telefonico del usuario
     * @param email correo electronico del usuario
     */

    public Usuario(String nombre, String telefono, String email) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.cliente = new Cliente();
        this.conductor = null;
        this.rolActivo = RolUsuario.CLIENTE;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public RolUsuario getRolActivo() {
        return rolActivo;
    }

    /**
     * Cambia el rol del usuario entre cliente y conductor dependiendo la situacion.
     *
     * @param rolNuevo el rol actual que el sistema usara para identificar al usuario
     */
    public void cambiarRolActivo(RolUsuario rolNuevo) {
        this.rolActivo = rolNuevo;
    }

    /**
     * Registra al usuario como un conductor.
     *
     * @param licencia licencia del conductor
     * @param vehiculo primer vehiculo a registrar del conductor
     */
    public void altaConductor(String licencia, Vehiculo vehiculo) {
        this.conductor = new Conductor(licencia);
        this.conductor.agregarVehiculo(vehiculo);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(nombre, usuario.nombre) && Objects.equals(telefono, usuario.telefono);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, telefono);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", rolActivo=" + rolActivo +
                ", cliente=" + cliente +
                ", conductor=" + conductor +
                '}';
    }
}
