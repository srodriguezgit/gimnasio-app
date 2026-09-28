package ar.argentech.ui;

import ar.argentech.domain.Pago;
import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.scene.control.Alert;

public class NuevoSocioController {private final NuevoSocioView view;
  private final SocioService socioService;
  private final PlanService planService;

  public NuevoSocioController(NuevoSocioView view, SocioService socioService, PlanService planService) {
    this.view = view;
    this.socioService = socioService;
    this.planService = planService;

    cargarPlanes();
    configurarEventos();
  }

  private void cargarPlanes() {
    view.cmbPlan.getItems().addAll(planService.obtenerPlanes());
  }

  private void configurarEventos() {

    view.btnRegistrar.setOnAction(e -> registrar());

    view.btnCancelar.setOnAction(e ->
        view.getScene().getWindow().hide()
    );

    view.cmbPlan.setOnAction(e -> {
      Plan plan = view.cmbPlan.getValue();
      if (plan != null) {
        view.txtMonto.setText(plan.getCosto().toString());
      }
    });
  }

  private void registrar() {
    try {
      view.btnRegistrar.setDisable(true);

      Socio socio = new Socio(
          null,
          view.txtNombre.getText(),
          view.txtApellido.getText(),
          view.txtDni.getText(),
          null,
          view.cmbPlan.getValue(),
          LocalDate.now(),
          null,
          null,
          new ArrayList<>(),
          new ArrayList<>()
      );

      Pago pago = new Pago(
          null,
          view.dpFechaPago.getValue(),
          new BigDecimal(
              view.txtMonto.getText().trim().replace(",", ".")
          ),
          view.cmbMetodoPago.getValue()
      );

      socioService.altaSocioConPago(socio, pago);

      BigDecimal saldo = socio.getPlanActual().getCosto()
          .subtract(pago.getMonto());

      new Alert(
          Alert.AlertType.INFORMATION,
          "Socio registrado. Saldo pendiente: $"
              + saldo.toPlainString()
      ).showAndWait();

      view.btnRegistrar.setDisable(true);
      view.btnRegistrar.setText("Socio registrado");
      view.btnCancelar.setText("Cerrar");

    } catch (NumberFormatException e) {
      view.btnRegistrar.setDisable(false);

      new Alert(
          Alert.AlertType.ERROR,
          "Ingresá un monto válido."
      ).showAndWait();

    } catch (RuntimeException e) {
      view.btnRegistrar.setDisable(false);

      new Alert(
          Alert.AlertType.ERROR,
          e.getMessage()
      ).showAndWait();
    }
  }
}