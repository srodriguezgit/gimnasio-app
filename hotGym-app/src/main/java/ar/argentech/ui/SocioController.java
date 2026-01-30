package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import ar.argentech.ui.pagos.RegistrarPagoController;
import ar.argentech.ui.pagos.RegistrarPagoView;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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

    TableColumn<Socio, String> colVencimiento = new TableColumn<>("Fecha Vencimiento");
    colVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaProximoVencimiento"));

    view.tablaSocios.getColumns().addAll(colDni, colNombre, colApellido, colVencimiento);
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

    view.btnCongelar.setOnAction(e -> {
      Socio seleccionado = view.tablaSocios.getSelectionModel().getSelectedItem();
      if (seleccionado == null) {
        new Alert(Alert.AlertType.WARNING, "Seleccioná un socio").showAndWait();
        return;
      }

      abrirModalCongelar(seleccionado);
    });

    view.btnRegistrarPago.setOnAction(e -> {
      Socio seleccionado = view.tablaSocios.getSelectionModel().getSelectedItem();
      if (seleccionado == null) {
        new Alert(Alert.AlertType.WARNING, "Seleccioná un socio").showAndWait();
        return;
      }

      abrirModalRegistrarPago(seleccionado);
    });

    view.btnEliminar.disableProperty().bind(
        view.tablaSocios.getSelectionModel().selectedItemProperty().isNull()
    );

    view.btnEliminar.setOnAction(e -> {
      Socio seleccionado = view.tablaSocios.getSelectionModel().getSelectedItem();
      if (seleccionado == null) return;

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirmar eliminación");
      confirm.setHeaderText("¿Eliminar al socio " + seleccionado.getNombre() + " " + seleccionado.getApellido() + "?");
      confirm.setContentText("Esta acción no se puede deshacer.");

      ButtonType btnSi = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
      ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
      confirm.getButtonTypes().setAll(btnSi, btnNo);

      confirm.showAndWait().ifPresent(res -> {
        if (res == btnSi) {
          boolean eliminado = socioService.eliminarPorId(seleccionado.getId());
          if (!eliminado) {
            new Alert(Alert.AlertType.ERROR, "No se pudo eliminar (no encontrado).").showAndWait();
          }

          // refrescar vista actual
          if (mostrandoMorosos) mostrarMorosos();
          else mostrarTodos();
        }
      });
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

  private void abrirModalCongelar(Socio socio) {
    CongelarCuotaView v = new CongelarCuotaView();

    Runnable refrescar = () -> {
      if (mostrandoMorosos) mostrarMorosos();
      else mostrarTodos();
    };

    new CongelarCuotaController(v, socioService, socio, refrescar);

    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Congelar cuota");
    modal.setScene(new Scene(v, 450, 260));
    modal.showAndWait();
  }

  private void abrirModalRegistrarPago(Socio socio) {

    // 1) Vista del modal
    RegistrarPagoView v = new RegistrarPagoView();

    // 2) Callback para refrescar la tabla cuando se registra el pago
    Runnable refrescar = () -> {
      if (mostrandoMorosos) {
        mostrarMorosos();
      } else {
        mostrarTodos();
      }
    };

    // 3) Controller del modal (conecta eventos y lógica)
    new RegistrarPagoController(v, socioService, socio, refrescar);

    // 4) Stage modal
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Registrar pago");
    modal.setScene(new Scene(v, 450, 260));

    // 5) Mostrar y esperar (bloquea hasta que se cierre)
    modal.showAndWait();
  }

}
