package ar.argentech.ui;

import ar.argentech.domain.Sesion;
import ar.argentech.domain.Usuario;
import ar.argentech.repository.AuthService;
import javafx.stage.Stage;

public class LoginController {

  private final LoginView view;
  private final AuthService auth;
  private final Runnable onSuccess;

  public LoginController(LoginView view, AuthService auth, Runnable onSuccess) {
    this.view = view;
    this.auth = auth;
    this.onSuccess = onSuccess;

    configurarEventos();
  }

  private void configurarEventos() {

    view.btnLogin.setOnAction(e -> intentarLogin());

    // Enter en password también loguea
    view.txtPass.setOnAction(e -> intentarLogin());
  }

  private void intentarLogin() {
    String user = view.txtUser.getText();
    String pass = view.txtPass.getText();

    Usuario u = auth.login(user, pass);
    if (u == null) {
      view.lblError.setText("Usuario o contraseña incorrectos.");
      return;
    }

    Sesion.setUsuarioActual(u);

    if (onSuccess != null) onSuccess.run();
  }

}