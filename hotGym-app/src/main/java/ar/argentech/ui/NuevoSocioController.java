package ar.argentech.ui;

import ar.argentech.domain.Pago;
import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import java.math.BigDecimal;
import java.time.LocalDate;

public class NuevoSocioController {private final NuevoSocioView view;
  private final SocioService socioService;
  private final PlanService planService;

  public NuevoSocioController(NuevoSocioView view, SocioService socioService, PlanService planService) {
    this.view = view;
    this.socioService = socioService;
    this.planService = new PlanService();

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
        null
    );

    Pago pago = new Pago(null,
        view.dpFechaPago.getValue(),
        new BigDecimal(view.txtMonto.getText()),
        view.cmbMetodoPago.getValue()
    );

    socioService.altaSocioConPago(socio, pago);
  }
}