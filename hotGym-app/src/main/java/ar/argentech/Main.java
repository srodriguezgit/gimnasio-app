package ar.argentech;

import ar.argentech.domain.DuracionPlan;
import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
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
    // Service
    SocioService socioService = new SocioService();

    // Datos de prueba
    Plan mensual = new Plan(null, "Mensual",
        new BigDecimal("40000"),
        DuracionPlan.MENSUAL);

    Socio s1 = new Socio(null, "Juan", "Perez", "30123456", null, mensual, LocalDate.now(), null, null, null);
    Socio s2 = new Socio(null, "Juan", "Carrillo", "28999888", null, mensual, LocalDate.now(), null, null, null);

    socioService.agregarSocio(s1);
    socioService.agregarSocio(s2);

    // UI
    MainView view = new MainView();
    new SocioController(view, socioService);

    Scene scene = new Scene(view, 800, 400);
    stage.setTitle("Gestión de Gimnasio");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
