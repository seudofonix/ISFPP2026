
package controlador;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import modelo.Modelo;
import usuario.RolUsuario;
import usuario.Usuario;

public class LoginController {

	private final Modelo modelo;
	private final Navegador navegador;

	@FXML private ToggleGroup grupoRol;
	@FXML private RadioButton rbUsuario;
	@FXML private RadioButton rbConductor;
	@FXML private ComboBox<Usuario> comboUsuario;
	@FXML private Label etiquetaDetalle;
	@FXML private Label etiquetaEstado;
	@FXML private Button botonIngresar;
	@FXML private Button botonRecargar;

	public LoginController(Modelo modelo, Navegador navegador) {

		this.modelo = modelo;
		this.navegador = navegador;
	}

	@FXML
	private void initialize() {

		comboUsuario.setConverter(new StringConverter<>() {

			@Override
			public String toString(Usuario u) {
				return u == null ? "" : u.getNombre() + " (" + u.getEmail() + ")";
			}

			@Override
			public Usuario fromString(String texto) {
				return null;
			}
		});

		grupoRol.selectedToggleProperty().addListener((obs, anterior, actual) -> {

			if (actual == null) {

				anterior.setSelected(true); // siempre debe haber un rol elegido
				return;
			}

			actualizarUsuarios();
		});

		comboUsuario.valueProperty().addListener((obs, anterior, actual) -> actualizarDetalle(actual));

		rbUsuario.setSelected(true);
		recargar();
	}

	@FXML
	private void recargar() {

		try {

			modelo.cargar();
			etiquetaEstado.setText("Datos cargados: " + modelo.getUsuarios().size() + " usuarios, "
					+ modelo.getVehiculos().size() + " vehiculos, " + modelo.getServicios().size() + " servicios");

		} catch (IOException | RuntimeException ex) {

			etiquetaEstado.setText("Error al cargar los datos");
			Alertas.error("No se pudieron cargar los datos", ex.getMessage());
		}

		actualizarUsuarios();
	}

	@FXML
	private void ingresar() {

		Usuario elegido = comboUsuario.getValue();

		if (elegido == null) {

			return;
		}

		RolUsuario rol = rolElegido();

		try {

			modelo.iniciarSesion(elegido, rol);

		} catch (IllegalStateException | IllegalArgumentException ex) {

			Alertas.error("No se pudo ingresar", ex.getMessage());
			return;
		}

		if (rol == RolUsuario.CONDUCTOR) {

			navegador.mostrarPanelConductor();

		} else {

			navegador.mostrarPanelCliente();
		}
	}

	private RolUsuario rolElegido() {

		return rbConductor.isSelected() ? RolUsuario.CONDUCTOR : RolUsuario.CLIENTE;
	}

	private void actualizarUsuarios() {

		comboUsuario.setItems(FXCollections.observableArrayList(modelo.getUsuariosConRol(rolElegido())));
		comboUsuario.getSelectionModel().clearSelection();
		comboUsuario.setPromptText(
				rolElegido() == RolUsuario.CONDUCTOR ? "Elegi un conductor" : "Elegi un usuario");
		actualizarDetalle(null);
	}

	private void actualizarDetalle(Usuario usuario) {

		botonIngresar.setDisable(usuario == null);

		if (usuario == null) {

			etiquetaDetalle.setText("");
			return;
		}

		if (rolElegido() == RolUsuario.CONDUCTOR && usuario.getConductor() != null) {

			etiquetaDetalle.setText("Licencia: " + usuario.getConductor().getLicenciaConducir() + "  |  Vehiculos: "
					+ String.join(", ", usuario.getConductor().getVehiculos().keySet()));

		} else {

			etiquetaDetalle.setText("Telefono: " + usuario.getTelefono());
		}
	}
}
