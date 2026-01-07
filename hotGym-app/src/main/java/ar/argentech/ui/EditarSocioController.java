package ar.argentech.ui;

import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class EditarSocioController {

  private final EditarSocioView view;
  private final Socio socio; // referencia al mismo objeto
  private final PlanService planService;
  private final Runnable onGuardado; // para refrescar al cerrar

  public EditarSocioController(
      EditarSocioView view,
      Socio socio,
      PlanService planService,
      Runnable onGuardado
  ) {
    this.view = view;
    this.socio = socio;
    this.planService = planService;
    this.onGuardado = onGuardado;

    cargarPlanes();
    view.cargarSocio(socio);
    configurarEventos();
  }

  private void cargarPlanes() {
    view.cmbPlan.getItems().setAll(planService.obtenerPlanes());
  }

  private void configurarEventos() {

    view.btnCancelar.setOnAction(e -> cerrar());

    view.btnGuardar.setOnAction(e -> guardar());
  }

  private void guardar() {
    if (view.txtNombre.getText().isBlank() || view.txtApellido.getText().isBlank()) {
      new Alert(Alert.AlertType.ERROR, "Nombre y apellido son obligatorios").showAndWait();
      return;
    }

    Plan plan = view.cmbPlan.getValue();
    if (plan == null) {
      new Alert(Alert.AlertType.ERROR, "Seleccioná un plan").showAndWait();
      return;
    }

    // Actualizamos el objeto socio (en memoria esto ya refleja en la tabla al refrescar)
    socio.setNombre(view.txtNombre.getText().trim());
    socio.setApellido(view.txtApellido.getText().trim());
    // socio.setDni(...) si decidís permitirlo
    socio.setPlanActual(plan);

    // Si querés: recalcular vencimiento si cambia plan y ya tenía ultimo pago:
    // if (socio.getFechaUltimoPago() != null) {
    //   socio.setFechaProximoVencimiento(plan.getDuracionPlan().calcularVencimiento(socio.getFechaUltimoPago()));
    // }

    new Alert(Alert.AlertType.INFORMATION, "Socio actualizado ✅").showAndWait();

    cerrar();
    if (onGuardado != null) onGuardado.run();
  }

  private void cerrar() {
    Stage stage = (Stage) view.getScene().getWindow();
    stage.close();
  }
}

