/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * VISTA del patron MVC. Solo construye y muestra los controles; no contiene logica de negocio ni
 * manejadores de eventos (eso lo cablea el Controlador).
 */
package vista;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import servicio.Servicio;
import usuario.Usuario;
import vehiculo.Vehiculo;

public class Vista {

	private final BorderPane raiz = new BorderPane();

	private final TableView<Vehiculo> tablaVehiculos = new TableView<>();
	private final TableView<Usuario> tablaUsuarios = new TableView<>();
	private final TableView<Servicio> tablaServicios = new TableView<>();

	private final Button botonRecargar = new Button("Recargar datos");
	private final Label etiquetaEstado = new Label("Sin datos cargados");

	private final ComboBox<Servicio> comboServicio = new ComboBox<>();
	private final TextField campoDistancia = new TextField();
	private final TextField campoDuracion = new TextField();
	private final Button botonCalcular = new Button("Calcular costo");
	private final Label etiquetaCosto = new Label("-");

	public Vista() {

		construirTablaVehiculos();
		construirTablaUsuarios();
		construirTablaServicios();

		TabPane pestanias = new TabPane(
				new Tab("Vehiculos", tablaVehiculos),
				new Tab("Usuarios", tablaUsuarios),
				new Tab("Servicios", tablaServicios),
				new Tab("Cotizador", construirCotizador()));
		pestanias.getTabs().forEach(t -> t.setClosable(false));

		HBox barra = new HBox(10, botonRecargar, etiquetaEstado);
		barra.setAlignment(Pos.CENTER_LEFT);
		barra.setPadding(new Insets(8));

		raiz.setTop(barra);
		raiz.setCenter(pestanias);
	}

	private void construirTablaVehiculos() {

		TableColumn<Vehiculo, String> patente = new TableColumn<>("Patente");
		patente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatente()));

		TableColumn<Vehiculo, String> modelo = new TableColumn<>("Modelo");
		modelo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelo()));

		TableColumn<Vehiculo, Number> capacidad = new TableColumn<>("Capacidad");
		capacidad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidadPasajeros()));

		TableColumn<Vehiculo, String> tipo = new TableColumn<>("Tipo");
		tipo.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoVehiculo())));

		TableColumn<Vehiculo, String> categoria = new TableColumn<>("Categoria");
		categoria.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getCategoriaVehiculo())));

		TableColumn<Vehiculo, String> servicios = new TableColumn<>("Servicios");
		servicios.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoServicio().toString()));

		tablaVehiculos.getColumns().addAll(patente, modelo, capacidad, tipo, categoria, servicios);
		tablaVehiculos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	private void construirTablaUsuarios() {

		TableColumn<Usuario, String> nombre = new TableColumn<>("Nombre");
		nombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));

		TableColumn<Usuario, String> telefono = new TableColumn<>("Telefono");
		telefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

		TableColumn<Usuario, String> email = new TableColumn<>("Email");
		email.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

		TableColumn<Usuario, String> rol = new TableColumn<>("Rol activo");
		rol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRolActivo())));

		TableColumn<Usuario, String> licencia = new TableColumn<>("Licencia");
		licencia.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getConductor() == null ? "-" : c.getValue().getConductor().getLicenciaConducir()));

		TableColumn<Usuario, String> vehiculos = new TableColumn<>("Vehiculos (patentes)");
		vehiculos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConductor() == null ? "-"
				: String.join(", ", c.getValue().getConductor().getVehiculos().keySet())));

		tablaUsuarios.getColumns().addAll(nombre, telefono, email, rol, licencia, vehiculos);
		tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	private void construirTablaServicios() {

		TableColumn<Servicio, String> nombre = new TableColumn<>("Nombre");
		nombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));

		TableColumn<Servicio, Number> base = new TableColumn<>("Tarifa base");
		base.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTarifaBase()));

		TableColumn<Servicio, Number> km = new TableColumn<>("Precio/km");
		km.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioKm()));

		TableColumn<Servicio, Number> minuto = new TableColumn<>("Precio/min");
		minuto.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioMinuto()));

		TableColumn<Servicio, String> tipoVehiculo = new TableColumn<>("Vehiculo");
		tipoVehiculo.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoVehiculo())));

		TableColumn<Servicio, String> categoria = new TableColumn<>("Categoria");
		categoria.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getCategoriaVehiculo())));

		TableColumn<Servicio, String> tipoServicio = new TableColumn<>("Servicio");
		tipoServicio.setCellValueFactory(
				c -> new SimpleStringProperty(String.valueOf(c.getValue().getTipoServicio())));

		tablaServicios.getColumns().addAll(nombre, base, km, minuto, tipoVehiculo, categoria, tipoServicio);
		tablaServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	private VBox construirCotizador() {

		comboServicio.setPromptText("Elegi un servicio");
		comboServicio.setPrefWidth(320);
		campoDistancia.setPromptText("Distancia (km)");
		campoDuracion.setPromptText("Duracion (min)");
		etiquetaCosto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		VBox caja = new VBox(10, new Label("Servicio"), comboServicio, campoDistancia, campoDuracion, botonCalcular,
				new Label("Costo estimado"), etiquetaCosto);
		caja.setPadding(new Insets(15));
		caja.setMaxWidth(340);
		return caja;
	}

	public BorderPane getRaiz() {

		return raiz;
	}

	public TableView<Vehiculo> getTablaVehiculos() {

		return tablaVehiculos;
	}

	public TableView<Usuario> getTablaUsuarios() {

		return tablaUsuarios;
	}

	public TableView<Servicio> getTablaServicios() {

		return tablaServicios;
	}

	public Button getBotonRecargar() {

		return botonRecargar;
	}

	public Label getEtiquetaEstado() {

		return etiquetaEstado;
	}

	public ComboBox<Servicio> getComboServicio() {

		return comboServicio;
	}

	public TextField getCampoDistancia() {

		return campoDistancia;
	}

	public TextField getCampoDuracion() {

		return campoDuracion;
	}

	public Button getBotonCalcular() {

		return botonCalcular;
	}

	public Label getEtiquetaCosto() {

		return etiquetaCosto;
	}
}
