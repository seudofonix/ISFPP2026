/**
 * Ignacio Bullon - Joel Erlan Hughes - Santiago Gittardi. ISFPP Programacion Orientada a Objetos 2026
 *
 * CONTROLADOR de Login.fxml. Primero se escribe el email; si existe, se elige como ingresar (usuario o
 * conductor). Ingresar como conductor exige que el email pertenezca a un conductor. handleIngresar activa el
 * rol, guarda el usuario en el contexto y abre el panel que corresponde.
 */
package ui.impl.javafx.controller;

import java.io.IOException;
import java.util.Optional;

import aplicacion.ContextoAplicacion;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.impl.javafx.VistasFX;
import usuario.RolUsuario;
import usuario.Usuario;

public class LoginController {

	private final ContextoAplicacion contexto;

	@FXML private Node panelEmail;
	@FXML private Node panelRol;
	@FXML private TextField txtEmail;
	@FXML private Label lblError;
	@FXML private Label lblSaludo;
	@FXML private Label lblDetalle;
	@FXML private Label lblEstado;

	private Usuario identificado;

	public LoginController(ContextoAplicacion contexto) {

		this.contexto = contexto;
	}

	@FXML
	private void initialize() {

		actualizarEstado();
		mostrarPasoEmail();
	}

	/** Paso 1: busca el email escrito entre los usuarios cargados. */
	@FXML
	private void handleContinuar() {

		String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

		if (email.isEmpty()) {

			lblError.setText("Escribi tu email para continuar.");
			return;
		}

		Optional<Usuario> usuario = contexto.buscarUsuarioPorEmail(email);

		if (usuario.isEmpty()) {

			lblError.setText("No existe ningun usuario con ese email.");
			return;
		}

		identificado = usuario.get();
		lblError.setText("");
		lblSaludo.setText("Hola, " + identificado.getNombre() + " (" + identificado.getEmail() + ")");
		panelEmail.setVisible(false);
		panelEmail.setManaged(false);
		panelRol.setVisible(true);
		panelRol.setManaged(true);
		lblDetalle.setText("Telefono: " + identificado.getTelefono());
	}

	@FXML
	private void handleCambiarMail() {

		mostrarPasoEmail();
	}

	@FXML
	private void handleIngresarComoUsuario() {

		ingresar(RolUsuario.CLIENTE);
	}

	@FXML
	private void handleIngresarComoConductor() {

		if (identificado == null || identificado.getConductor() == null) {

			Alertas.error("No se pudo ingresar", "Este mail no pertenece a un conductor.");
			return;
		}

		ingresar(RolUsuario.CONDUCTOR);
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

		mostrarPasoEmail(); // los objetos se crearon de nuevo: hay que identificarse otra vez
	}

	private void ingresar(RolUsuario rol) {

		try {

			if (identificado.getRolActivo() != rol) {

				identificado.cambiarRolActivo(rol);
			}

			contexto.setUsuarioLogueado(identificado);

		} catch (IllegalStateException | IllegalArgumentException ex) {

			Alertas.error("No se pudo ingresar", ex.getMessage());
			return;
		}

		Stage escenario = (Stage) txtEmail.getScene().getWindow();

		if (rol == RolUsuario.CONDUCTOR) {

			VistasFX.mostrar(escenario, "PanelConductor.fxml", "Movilidad Digital - Conductor", contexto);

		} else {

			VistasFX.mostrar(escenario, "PanelCliente.fxml", "Movilidad Digital - Usuario", contexto);
		}
	}

	private void mostrarPasoEmail() {

		identificado = null;
		txtEmail.clear();
		lblError.setText("");
		lblDetalle.setText("");
		panelRol.setVisible(false);
		panelRol.setManaged(false);
		panelEmail.setVisible(true);
		panelEmail.setManaged(true);
		txtEmail.requestFocus();
	}

	private void actualizarEstado() {

		lblEstado.setText("Datos cargados desde " + contexto.getOrigenDatos() + ": " + contexto.getUsuarios().size()
				+ " usuarios, " + contexto.getVehiculos().size() + " vehiculos, " + contexto.getServicios().size()
				+ " servicios");
	}
}
