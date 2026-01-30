package ar.argentech.ui.pagos;

import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class RegistrarPagoController {

  private final RegistrarPagoView view;
  private final SocioService socioService;
  private final Socio socio;
  private final Runnable onRegistrado;

  public RegistrarPagoController(
      RegistrarPagoView view,
      SocioService socioService,
      Socio socio,
      Runnable onRegistrado
  ) {
    this.view = view;
    this.socioService = socioService;
    this.socio = socio;
    this.onRegistrado = onRegistrado;

    // Info arriba
    view.lblSocio.setText(socio.getNombre() + " " + socio.getApellido() + " (DNI " + socio.getDni() + ")");

    // Precargar monto con el costo del plan si existe
    if (socio.getPlanActual() != null && socio.getPlanActual().getCosto() != null) {
      view.txtMonto.setText(socio.getPlanActual().getCosto().toString());
    }

    configurarEventos();
  }

  private void configurarEventos() {

    view.btnCancelar.setOnAction(e -> cerrar());

    view.btnRegistrar.setOnAction(e -> registrar());
  }

  private void registrar() {

    LocalDate fecha = view.dpFechaPago.getValue();
    if (fecha == null) {
      new Alert(Alert.AlertType.ERROR, "Seleccioná una fecha de pago.").showAndWait();
      return;
    }

    if (view.cmbMetodoPago.getValue() == null) {
      new Alert(Alert.AlertType.ERROR, "Seleccioná un método de pago.").showAndWait();
      return;
    }

    BigDecimal monto;
    try {
      String raw = view.txtMonto.getText().trim().replace(",", ".");
      monto = new BigDecimal(raw);
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Monto inválido.").showAndWait();
      return;
    }

    if (monto.compareTo(BigDecimal.ZERO) <= 0) {
      new Alert(Alert.AlertType.ERROR, "El monto debe ser mayor a 0.").showAndWait();
      return;
    }

    Pago pago = new Pago(
        null,
        fecha,
        monto,
        view.cmbMetodoPago.getValue()
    );

    try {
      socioService.registrarPago(socio.getDni(), pago);

      new Alert(Alert.AlertType.INFORMATION, "Pago registrado ✅").showAndWait();

      cerrar();
      if (onRegistrado != null) onRegistrado.run();

    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
    }
  }

  private void cerrar() {
    Stage stage = (Stage) view.getScene().getWindow();
    stage.close();
  }
}
