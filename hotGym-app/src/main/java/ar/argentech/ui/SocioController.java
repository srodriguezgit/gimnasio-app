package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class SocioController {

  private final MainView view;
  private final SocioService socioService;

  private final ObservableList<Socio> sociosObservable =
      FXCollections.observableArrayList();

  public SocioController(MainView view, SocioService socioService) {
    this.view = view;
    this.socioService = socioService;

    configurarTabla();
    cargarTodos();
    configurarEventos();
  }

  private void configurarTabla() {

    TableColumn<Socio, String> colDni = new TableColumn<>("DNI");
    colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

    TableColumn<Socio, String> colNombre = new TableColumn<>("Nombre");
    colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

    TableColumn<Socio, String> colApellido = new TableColumn<>("Apellido");
    colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));

    view.tablaSocios.getColumns().addAll(colDni, colNombre, colApellido);
    view.tablaSocios.setItems(sociosObservable);
  }

  private void cargarTodos() {
    sociosObservable.setAll(socioService.obtenerTodos());
  }

  private void configurarEventos() {
    view.btnBuscar.setOnAction(e -> {
      String texto = view.txtBuscar.getText().trim();
      sociosObservable.setAll(socioService.buscar(texto));
    });
  }
}
