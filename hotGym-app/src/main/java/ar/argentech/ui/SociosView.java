package ar.argentech.ui;

import ar.argentech.domain.Socio;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SociosView extends BorderPane {

  public TextField txtBuscar = new TextField();
  public Button btnBuscar = new Button("Buscar");
  public TableView<Socio> tablaSocios = new TableView<>();

  public SociosView() {

    txtBuscar.setPromptText("Buscar por nombre o DNI");

    HBox topBar = new HBox(10, txtBuscar, btnBuscar);
    topBar.setPadding(new Insets(10));

    setTop(topBar);
    setCenter(tablaSocios);
  }

}
