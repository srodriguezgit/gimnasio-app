package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class CongelarCuotaController {

  private final CongelarCuotaView view;
  private final SocioService socioService;
  private final Socio socio;
  private final Runnable onAplicado;

  public CongelarCuotaController(
      CongelarCuotaView view,
      SocioService socioService,
      Socio socio,
      Runnable onAplicado
  ) {
    this.view = view;
    this.socioService = socioService;
    this.socio = socio;
    this.onAplicado = onAplicado;

    configurarEventos();
  }

  private void configurarEventos() {
    view.btnCancelar.setOnAction(e -> cerrar());

    view.btnConfirmar.setOnAction(e -> aplicar());
  }

  private void aplicar() {
    int dias = view.spDias.getValue();
    String motivo = view.txtMotivo.getText().trim();
    String autorizadoPor = view.txtAutorizadoPor.getText().trim();

    if (autorizadoPor.isBlank()) {
      new Alert(Alert.AlertType.ERROR, "Debe indicar quién autoriza (usuario/encargado).").showAndWait();
      return;
    }

    if (dias <= 0) {
      new Alert(Alert.AlertType.ERROR, "Los días deben ser mayores a 0.").showAndWait();
      return;
    }

    try {
      socioService.congelarCuota(socio.getDni(), dias, motivo, autorizadoPor);
      new Alert(Alert.AlertType.INFORMATION, "Congelación aplicada ✅").showAndWait();
      cerrar();
      if (onAplicado != null) onAplicado.run();
    } catch (SecurityException se) {
      new Alert(Alert.AlertType.ERROR, "No autorizado.").showAndWait();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
    }
  }

  private void cerrar() {
    Stage stage = (Stage) view.getScene().getWindow();
    stage.close();
  }

}
