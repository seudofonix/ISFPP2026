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

        Scanner entrada = new Scanner(new File(nombreArchivo));

        while (entrada.hasNextLine()) {
            String linea = entrada.nextLine().trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            String[] partes = linea.split(";");
            String patente = partes[0];
            String modelo = partes[1];
            int capacidad = Integer.parseInt(partes[2]);
            TipoVehiculo tipo = TipoVehiculo.valueOf(partes[3]);
            CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(partes[4]);

            Set<TipoServicio> servicios = new HashSet<>();
            for (int i = 5; i < partes.length; i++) {
                if (!partes[i].isBlank()) {
                    servicios.add(TipoServicio.valueOf(partes[i]));
                }
            }

            Vehiculo vehiculo = new Vehiculo(patente, modelo, categoria, tipo, servicios);

            vehiculos.put(patente, vehiculo);
        }
        entrada.close();

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

        Scanner entrada = new Scanner(new File(nombreArchivo));
        while (entrada.hasNextLine()) {
            String linea = entrada.nextLine().trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            String[] partes = linea.split(";");
            String nombre = partes[0];
            String telefono = partes[1];
            String email = partes[2];

            Usuario usuario = new Usuario(nombre, telefono, email);

            if (partes.length > 4 && !partes[3].isBlank()) {
                String licencia = partes[3];
                Vehiculo primerVehiculo = vehiculos.get(partes[4]);

                usuario.altaConductor(licencia, primerVehiculo);

                for (int i = 5; i < partes.length; i++) {
                    if (!partes[i].isBlank()) {
                        Vehiculo v = vehiculos.get(partes[i]);
                        if (v != null) {
                            usuario.getConductor().agregarVehiculo(v);
                        }
                    }
                }
            }
            usuarios.put(email, usuario);
        }
        entrada.close();

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

        Scanner entrada = new Scanner(new File(nombreArchivo));
        while (entrada.hasNextLine()) {
            String linea = entrada.nextLine().trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            String[] partes = linea.split(";");
            String nombre = partes[0];
            double tarifaBase = Double.parseDouble(partes[1]);
            double precioKm = Double.parseDouble(partes[2]);
            double precioMinuto = Double.parseDouble(partes[3]);
            TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[4]);
            CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[5]);
            TipoServicio tipoServicio = TipoServicio.valueOf(partes[6]);

            Servicio servicio = new Servicio(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo, categoriaVehiculo, tipoServicio);
            servicios.add(servicio);
        }
        entrada.close();

        return servicios;
    }
}
