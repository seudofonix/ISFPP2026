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
import idioma.Mensajes;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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
	@FXML private ComboBox<String> cmbIdioma;

	private Usuario identificado;

	public LoginController(ContextoAplicacion contexto) {

		this.contexto = contexto;
	}

	@FXML
	private void initialize() {

		actualizarEstado();
		mostrarPasoEmail();
		prepararSelectorIdioma();
	}

	/** Combo con los idiomas disponibles; al elegir otro se recarga la pantalla en ese idioma. */
	private void prepararSelectorIdioma() {

		cmbIdioma.getItems().setAll(Mensajes.IDIOMAS);
		cmbIdioma.setConverter(new StringConverter<>() {

			@Override
			public String toString(String codigo) {

				return codigo == null ? "" : Mensajes.nombreDeIdioma(codigo);
			}

			@Override
			public String fromString(String texto) {

				return null;
			}
		});
		cmbIdioma.setValue(Mensajes.getIdioma().getLanguage());
		cmbIdioma.setOnAction(e -> {

			String elegido = cmbIdioma.getValue();

			if (elegido != null && !elegido.equals(Mensajes.getIdioma().getLanguage())) {

				Mensajes.setIdioma(elegido);
				VistasFX.mostrar((Stage) txtEmail.getScene().getWindow(), "Login.fxml", Mensajes.get("ventana.login"),
						contexto);
			}
		});
	}

	/** Paso 1: busca el email escrito entre los usuarios cargados. */
	@FXML
	private void handleContinuar() {

		String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

		if (email.isEmpty()) {

			lblError.setText(Mensajes.get("login.escribiEmail"));
			return;
		}

		Optional<Usuario> usuario = contexto.buscarUsuarioPorEmail(email);

		if (usuario.isEmpty()) {

			lblError.setText(Mensajes.get("login.mailDesconocido"));
			return;
		}

		identificado = usuario.get();
		lblError.setText("");
		lblSaludo.setText(Mensajes.get("login.hola", identificado.getNombre(), identificado.getEmail()));
		panelEmail.setVisible(false);
		panelEmail.setManaged(false);
		panelRol.setVisible(true);
		panelRol.setManaged(true);
		lblDetalle.setText(Mensajes.get("login.telefono", identificado.getTelefono()));
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

			Alertas.error(Mensajes.get("login.noSePudoIngresarTitulo"), Mensajes.get("login.noEsConductor"));
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

			lblEstado.setText(Mensajes.get("login.errorCarga", contexto.getOrigen()));
			Alertas.error(Mensajes.get("login.errorCargaTitulo"), ex.getMessage());
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

			Alertas.error(Mensajes.get("login.noSePudoIngresarTitulo"), ex.getMessage());
			return;
		}

		Stage escenario = (Stage) txtEmail.getScene().getWindow();

		if (rol == RolUsuario.CONDUCTOR) {

			VistasFX.mostrar(escenario, "PanelConductor.fxml", Mensajes.get("ventana.conductor"), contexto);

		} else {

			VistasFX.mostrar(escenario, "PanelCliente.fxml", Mensajes.get("ventana.usuario"), contexto);
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

		lblEstado.setText(Mensajes.get("login.estadoDatos", contexto.getOrigen(), contexto.getUsuarios().size(),
				contexto.getVehiculos().size(), contexto.getServicios().size()));
	}
}
