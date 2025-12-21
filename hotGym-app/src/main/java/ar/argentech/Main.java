package ar.argentech;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage stage) {
    Label label = new Label("Sistema de Gestión de Gimnasio");
    Scene scene = new Scene(label, 400, 200);
    stage.setTitle("Hot Gym");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
