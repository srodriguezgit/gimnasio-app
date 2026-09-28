package ar.argentech.ui;

import ar.argentech.domain.Socio;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SociosView extends BorderPane {

  public TextField txtBuscar = new TextField();
  public Button btnBuscar = new Button("Buscar");
  public Button btnEditar = new Button("Editar");
  public Button btnCongelar = new Button("Congelar");
  public Button btnRegistrarPago = new Button("Registrar Pago");
  public Button btnHistorial = new Button("Ver historial");
  public Button btnVencimiento = new Button("Ajustar vencimiento");
  public Button btnEliminar = new Button("Eliminar");
  public TableView<Socio> tablaSocios = new TableView<>();

  public SociosView() {

    txtBuscar.setPromptText("Buscar por nombre o DNI");

    HBox busqueda = new HBox(
        10,
        txtBuscar,
        btnBuscar,
        btnHistorial
    );

    HBox acciones = new HBox(
        10,
        btnEditar,
        btnCongelar,
        btnRegistrarPago,
        btnVencimiento,
        btnEliminar
    );

    VBox encabezado = new VBox(10, busqueda, acciones);
    encabezado.setPadding(new Insets(10));

    setTop(encabezado);
    setCenter(tablaSocios);
  }

}
