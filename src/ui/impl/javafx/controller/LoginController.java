/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de Login.fxml. Se elige primero el rol (usuario o conductor), despues la cuenta (la lista se
 * filtra segun el rol) y handleIngresar activa el rol, guarda el usuario en el contexto y abre el panel.
 */
package ui.impl.javafx.controller;

import java.io.IOException;

import aplicacion.ContextoAplicacion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import logica.Movilidad;
import ui.impl.javafx.VistasFX;
import usuario.RolUsuario;
import usuario.Usuario;

public class LoginController {

	private final ContextoAplicacion contexto;

	@FXML private ToggleGroup grupoRol;
	@FXML private RadioButton rbUsuario;
	@FXML private RadioButton rbConductor;
	@FXML private ComboBox<Usuario> cmbUsuarios;
	@FXML private Label lblDetalle;
	@FXML private Label lblEstado;
	@FXML private Button btnIngresar;

	public LoginController(ContextoAplicacion contexto) {

		this.contexto = contexto;
	}

	@FXML
	private void initialize() {

		cmbUsuarios.setConverter(new StringConverter<>() {

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

			cargarUsuarios();
		});

		cmbUsuarios.valueProperty().addListener((obs, anterior, actual) -> actualizarDetalle(actual));

		rbUsuario.setSelected(true);
		actualizarEstado();
		cargarUsuarios();
	}

	@FXML
	private void handleRecargarDatos() {

		try {

			contexto.recargarDatos();
			actualizarEstado();

		} catch (IOException | RuntimeException ex) {

			lblEstado.setText("Error al cargar los datos desde " + contexto.getOrigenDatos());
			Alertas.error("No se pudieron cargar los datos", ex.getMessage());
		}

		cargarUsuarios();
	}

	@FXML
	private void handleIngresar() {

		Usuario elegido = cmbUsuarios.getValue();

		if (elegido == null) {

			Alertas.advertencia("Falta elegir la cuenta", "Elegi un usuario de la lista.");
			return;
		}

		RolUsuario rol = rolElegido();

		try {

			if (elegido.getRolActivo() != rol) {

				elegido.cambiarRolActivo(rol);
			}

			contexto.setUsuarioLogueado(elegido);

		} catch (IllegalStateException | IllegalArgumentException ex) {

			Alertas.error("No se pudo ingresar", ex.getMessage());
			return;
		}

		Stage escenario = (Stage) btnIngresar.getScene().getWindow();

		if (rol == RolUsuario.CONDUCTOR) {

			VistasFX.mostrar(escenario, "PanelConductor.fxml", "Movilidad Digital - Conductor", contexto);

		} else {

			VistasFX.mostrar(escenario, "PanelCliente.fxml", "Movilidad Digital - Usuario", contexto);
		}
	}

	private RolUsuario rolElegido() {

		return rbConductor.isSelected() ? RolUsuario.CONDUCTOR : RolUsuario.CLIENTE;
	}

	private void cargarUsuarios() {

		var lista = rolElegido() == RolUsuario.CONDUCTOR ? contexto.getUsuariosConRol(RolUsuario.CONDUCTOR)
				: Movilidad.filtrarClientesDisponibles(contexto.getUsuarios());

		cmbUsuarios.setItems(FXCollections.observableArrayList(lista));
		cmbUsuarios.getSelectionModel().clearSelection();
		cmbUsuarios.setPromptText(rolElegido() == RolUsuario.CONDUCTOR ? "Elegi un conductor" : "Elegi un usuario");
		actualizarDetalle(null);
	}

	private void actualizarEstado() {

		lblEstado.setText("Datos cargados desde " + contexto.getOrigenDatos() + ": " + contexto.getUsuarios().size()
				+ " usuarios, " + contexto.getVehiculos().size() + " vehiculos, " + contexto.getServicios().size()
				+ " servicios");
	}

	private void actualizarDetalle(Usuario usuario) {

		btnIngresar.setDisable(usuario == null);

		if (usuario == null) {

			lblDetalle.setText("");

		} else if (rolElegido() == RolUsuario.CONDUCTOR && usuario.getConductor() != null) {

			lblDetalle.setText("Licencia: " + usuario.getConductor().getLicenciaConducir() + "  |  Vehiculos: "
					+ String.join(", ", usuario.getConductor().getVehiculos().keySet()));

		} else {

			lblDetalle.setText("Telefono: " + usuario.getTelefono());
		}
	}
}
