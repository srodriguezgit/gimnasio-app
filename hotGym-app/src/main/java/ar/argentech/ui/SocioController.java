package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SocioController {

  private final SociosView view;
  private final SocioService socioService;
  private final PlanService planService;

  private boolean mostrandoMorosos = false;

  private final ObservableList<Socio> sociosObservable =
      FXCollections.observableArrayList();

  public SocioController(SociosView  view, SocioService socioService, PlanService planService) {
    this.view = view;
    this.socioService = socioService;
    this.planService = planService;

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

    view.btnEditar.setOnAction(e -> {
      Socio seleccionado = view.tablaSocios.getSelectionModel().getSelectedItem();
      if (seleccionado == null) {
        new Alert(
            Alert.AlertType.WARNING,
            "Seleccioná un socio para editar"
        ).showAndWait();
        return;
      }

      abrirModalEditar(seleccionado);
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

  private void abrirModalEditar(Socio seleccionado) {

    EditarSocioView editView = new EditarSocioView();

    // callback para refrescar según el filtro actual
    Runnable refrescar = () -> {
      if (mostrandoMorosos) mostrarMorosos();
      else mostrarTodos();
    };

    new EditarSocioController(editView, seleccionado, planService, refrescar);

    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Editar socio");
    modal.setScene(new Scene(editView, 450, 320));
    modal.showAndWait();
  }

}
