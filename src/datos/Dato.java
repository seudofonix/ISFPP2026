/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga los datos del sistema desde archivos de texto.
 */
package datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import servicio.*;
import usuario.*;
import vehiculo.*;

public class Dato {

    /**
     * Carga los vehiculos desde el archivo de texto, separa cada campo con ; e ignora el encabezado que arranca con #.
     *
     * @return mapa indexado por patente
     */
    public static HashMap<String, Vehiculo> cargarVehiculos(String nombreArchivo)
            throws FileNotFoundException {

        HashMap<String, Vehiculo> vehiculos = new HashMap<>();

        try (Scanner entrada = new Scanner(new File(nombreArchivo))) {
            while (entrada.hasNextLine()) {
                String linea = entrada.nextLine().trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                String[] partes = linea.split(";");
                if (partes.length < 5) {
                    continue;
                }
                String patente = partes[0].trim();
                String modelo = partes[1].trim();
                int capacidad = Integer.parseInt(partes[2].trim());
                TipoVehiculo tipo = TipoVehiculo.valueOf(partes[3].trim());
                CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(partes[4].trim());

                Set<TipoServicio> servicios = new HashSet<>();
                for (int i = 5; i < partes.length; i++) {
                    String servicioStr = partes[i].trim();
                    if (!servicioStr.isBlank()) {
                        servicios.add(TipoServicio.valueOf(servicioStr));
                    }
                }

                Vehiculo vehiculo = new Vehiculo(patente, modelo, capacidad, categoria, tipo, servicios);

                vehiculos.put(patente, vehiculo);
            }
        }

        return vehiculos;
    }

    /**
     * Carga los usuarios desde el archivo de texto, separa cada campo con ; e ignora el encabezado que arranca con #.
     * Si el usuario cuenta con +4 campos, es tambien un conductor y los siguienes espacios son usados para guardar las patentes de sus vehiculos.
     *
     * @param vehiculos mapa de vehículos ya cargados
     * @return mapa indexado por email
     */
    public static HashMap<String, Usuario> cargarUsuarios(String nombreArchivo,
            HashMap<String, Vehiculo> vehiculos)
            throws FileNotFoundException {

        HashMap<String, Usuario> usuarios = new HashMap<>();

        try (Scanner entrada = new Scanner(new File(nombreArchivo))) {
            while (entrada.hasNextLine()) {
                String linea = entrada.nextLine().trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                String[] partes = linea.split(";");
                if (partes.length < 3) {
                    continue;
                }
                String nombre = partes[0].trim();
                String telefono = partes[1].trim();
                String email = partes[2].trim();

                Usuario usuario = new Usuario(nombre, telefono, email);

                if (partes.length > 4 && !partes[3].isBlank()) {
                    String licencia = partes[3].trim();
                    Vehiculo primerVehiculo = null;
                    int primerIndice = -1;

                    for (int i = 4; i < partes.length; i++) {
                        String patente = partes[i].trim();
                        if (!patente.isBlank()) {
                            Vehiculo v = vehiculos.get(patente);
                            if (v != null) {
                                primerVehiculo = v;
                                primerIndice = i;
                                break;
                            }
                        }
                    }

                    if (primerVehiculo != null) {
                        usuario.altaConductor(licencia, primerVehiculo);

                        for (int i = primerIndice + 1; i < partes.length; i++) {
                            String patente = partes[i].trim();
                            if (!patente.isBlank()) {
                                Vehiculo v = vehiculos.get(patente);
                                if (v != null) {
                                    usuario.getConductor().agregarVehiculo(v);
                                }
                            }
                        }
                    }
                }
                usuarios.put(email, usuario);
            }
        }

        return usuarios;
    }

    /**
     * Carga los serivcios desde el archivo de texto, selecciona cada campo separado con ; e ignora el encabezado que arranca con #
     *
     * @return lista de servicios
     */
    public static ArrayList<Servicio> cargarServicios(String nombreArchivo)
            throws FileNotFoundException {

        ArrayList<Servicio> servicios = new ArrayList<>();

        try (Scanner entrada = new Scanner(new File(nombreArchivo))) {
            while (entrada.hasNextLine()) {
                String linea = entrada.nextLine().trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                String[] partes = linea.split(";");
                if (partes.length < 7) {
                    continue;
                }
                String nombre = partes[0].trim();
                double tarifaBase = Double.parseDouble(partes[1].trim());
                double precioKm = Double.parseDouble(partes[2].trim());
                double precioMinuto = Double.parseDouble(partes[3].trim());
                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[4].trim());
                CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[5].trim());
                TipoServicio tipoServicio = TipoServicio.valueOf(partes[6].trim());

                Servicio servicio = new Servicio(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo, categoriaVehiculo, tipoServicio);
                servicios.add(servicio);
            }
        }

        return servicios;
    }
}
