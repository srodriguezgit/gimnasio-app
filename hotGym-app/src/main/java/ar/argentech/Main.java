package ar.argentech;

import ar.argentech.domain.DuracionPlan;
import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.repository.AuthService;
import ar.argentech.repository.UsuarioRepository;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import ar.argentech.ui.LoginController;
import ar.argentech.ui.LoginView;
import ar.argentech.ui.MainController;
import ar.argentech.ui.MainView;
import ar.argentech.ui.SocioController;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage stage) {

    // 1) Inicializar base de datos
    DbInit.init();

    // 2) Servicios de autenticación
    UsuarioRepository usuarioRepository = new UsuarioRepository();
    AuthService authService = new AuthService(usuarioRepository);

    // 3) Vista login dentro de la misma ventana
    LoginView loginView = new LoginView();

    Runnable onSuccess = () -> mostrarApp(stage);

    new LoginController(loginView, authService, onSuccess);

    Scene loginScene = new Scene(loginView, 900, 500);
    stage.setTitle("Gestión de Gimnasio - Login");
    stage.setScene(loginScene);
    stage.show();
  }

  private void mostrarApp(Stage stage) {

    SocioService socioService = new SocioService();
    PlanService planService = new PlanService();

    MainView mainView = new MainView();
    new MainController(mainView, socioService, planService);

    Scene mainScene = new Scene(mainView, 900, 500);
    stage.setTitle("Gestión de Gimnasio");
    stage.setScene(mainScene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
