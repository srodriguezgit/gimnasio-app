package ar.argentech.ui;

import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import javafx.scene.control.Alert;

public class EditarSocioController {

  private final EditarSocioView view;
  private final Socio socio;
  private final SocioService socioService;
  private final Runnable onGuardado;

  public EditarSocioController(
      EditarSocioView view,
      Socio socio,
      PlanService planService,
      SocioService socioService,
      Runnable onGuardado
  ) {
    this.view = view;
    this.socio = socio;
    this.socioService = socioService;
    this.onGuardado = onGuardado;

    view.cmbPlan.getItems().setAll(
        planService.obtenerPlanes()
    );

    Plan actual = socio.getPlanActual();

    // Permite conservar el plan actual aunque haya sido desactivado.
    if (actual != null
        && view.cmbPlan.getItems().stream().noneMatch(
        p -> p.getId().equals(actual.getId())
    )) {
      view.cmbPlan.getItems().add(actual);
    }

    view.cargarSocio(socio);

    if (actual != null) {
      view.cmbPlan.getItems().stream()
          .filter(p -> p.getId().equals(actual.getId()))
          .findFirst()
          .ifPresent(p -> view.cmbPlan.setValue(p));
    }

    view.btnCancelar.setOnAction(
        e -> view.getScene().getWindow().hide()
    );

    view.btnGuardar.setOnAction(e -> guardar());
  }

  private void guardar() {
    try {
      view.btnGuardar.setDisable(true);

      Plan plan = view.cmbPlan.getValue();

      socioService.actualizarDatosSocio(
          socio.getId(),
          view.txtNombre.getText(),
          view.txtApellido.getText(),
          view.txtDni.getText(),
          plan == null ? null : plan.getId()
      );

    } catch (RuntimeException e) {
      view.btnGuardar.setDisable(false);

      new Alert(
          Alert.AlertType.ERROR,
          e.getMessage()
      ).showAndWait();

      return;
    }

    view.getScene().getWindow().hide();

    if (onGuardado != null) {
      onGuardado.run();
    }
  }
}

