package ar.argentech.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class HomeView extends BorderPane {

  public HomeView() {

    VBox centerBox = new VBox(15);
    centerBox.setAlignment(Pos.CENTER);

    Label titulo = new Label("Gestión de Gimnasio");
    titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label subtitulo = new Label("Seleccione una sección del menú");
    subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

    // Si después querés logo:
    // ImageView logo = new ImageView(new Image("/logo.png"));

    centerBox.getChildren().addAll(titulo, subtitulo);

    setCenter(centerBox);
  }
}