/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * Carga las rutas de los archivos de datos desde config.properties.
 */
package datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {

    private static String archivoUsuarios;
    private static String archivoServicios;
    private static String archivoVehiculos;

    public static void parametros() throws IOException {
        Properties properties = new Properties();
        InputStream entrada = CargarParametros.class.getClassLoader().getResourceAsStream("config.properties");
        if (entrada == null) {
            File archivoConfig = new File("config.properties");
            if (!archivoConfig.exists()) {
                archivoConfig = new File("src/config.properties");
            }
            entrada = new FileInputStream(archivoConfig);
        }
        try (InputStream in = entrada) {
            properties.load(in);
            archivoUsuarios = properties.getProperty("usuario");
            archivoServicios = properties.getProperty("servicio");
            archivoVehiculos = properties.getProperty("vehiculo");
        }
    }

    public static String getArchivoUsuarios() {
        return archivoUsuarios;
    }

    public static String getArchivoServicios() {
        return archivoServicios;
    }

    public static String getArchivoVehiculos() {
        return archivoVehiculos;
    }
}
