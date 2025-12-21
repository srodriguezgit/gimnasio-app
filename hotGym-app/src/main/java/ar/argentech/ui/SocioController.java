package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class SocioController {

  private final SociosView view;
  private final SocioService socioService;

  private boolean mostrandoMorosos = false;

  private final ObservableList<Socio> sociosObservable =
      FXCollections.observableArrayList();

  public SocioController(SociosView  view, SocioService socioService) {
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

      if(mostrandoMorosos){
        sociosObservable.setAll(
            socioService.obtenerMorosos(LocalDate.now()).stream()
                .filter(s -> s.coincideCon(texto))
                .toList()
        );
      }else {
        sociosObservable.setAll(socioService.buscar(texto));
      }
    });
  }

  public void mostrarTodos(){
    mostrandoMorosos = false;
    sociosObservable.setAll(socioService.obtenerTodos());
  }

  public void mostrarMorosos() {
    mostrandoMorosos = true;
    sociosObservable.setAll(socioService.obtenerMorosos(LocalDate.now()));
  }

}
