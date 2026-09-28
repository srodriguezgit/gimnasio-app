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

    view.lblSocio.setText(
        socio.getNombre() + " " + socio.getApellido()
            + " (DNI " + socio.getDni() + ")"
    );

    try {
      var cuotas = socioService.obtenerCuotas(socio.getDni());

      view.cmbCuota.getItems().setAll(
          cuotas.stream()
              .filter(q -> q.getSaldo().signum() > 0)
              .toList()
      );

      view.cmbOperacion.valueProperty().addListener(
          (obs, anterior, actual) -> actualizarOperacion()
      );

      view.cmbCuota.valueProperty().addListener(
          (obs, anterior, actual) -> actualizarOperacion()
      );

      if (!view.cmbCuota.getItems().isEmpty()) {
        view.cmbCuota.getSelectionModel().selectFirst();
      }

      view.cmbOperacion.setValue(
          view.cmbCuota.getItems().isEmpty()
              ? "Renovar cuota"
              : "Abonar saldo"
      );

    } catch (RuntimeException e) {
      view.lblResumen.setText(
          "No se pudieron cargar las cuotas: " + e.getMessage()
      );

      view.btnRegistrar.setDisable(true);
    }

    configurarEventos();
  }

  private void actualizarOperacion() {
    boolean saldo = "Abonar saldo".equals(
        view.cmbOperacion.getValue()
    );

    view.cmbCuota.setDisable(!saldo);

    if (saldo) {
      var cuota = view.cmbCuota.getValue();

      view.btnRegistrar.setDisable(cuota == null);

      view.txtMonto.setText(
          cuota == null ? "" : cuota.getSaldo().toPlainString()
      );

      view.lblResumen.setText(
          cuota == null
              ? "No hay cuotas pendientes registradas."
              : "Importe acordado: $"
              + cuota.getImporte().toPlainString()
              + " · Abonado: $"
              + cuota.getAbonado().toPlainString()
              + " · Pendiente: $"
              + cuota.getSaldo().toPlainString()
              + "\nEste cobro no modifica el vencimiento."
      );

    } else {
      boolean sinPlan = socio.getPlanActual() == null;

      view.btnRegistrar.setDisable(sinPlan);

      view.txtMonto.setText(
          sinPlan
              ? ""
              : socio.getPlanActual().getCosto().toPlainString()
      );

      view.lblResumen.setText(
          sinPlan
              ? "El socio no tiene un plan."
              : "Cuota completa: $"
              + socio.getPlanActual().getCosto().toPlainString()
              + ". Podés abonar una parte: se habilita "
              + "el período y queda saldo pendiente."
              + "\nLos pagos anteriores a esta versión "
              + "no tienen deuda calculada."
      );
    }
  }

  private void configurarEventos() {
    view.btnCancelar.setOnAction(e -> cerrar());
    view.btnRegistrar.setOnAction(e -> registrar());
  }

  private void registrar() {
    LocalDate fecha = view.dpFechaPago.getValue();

    if (fecha == null) {
      new Alert(
          Alert.AlertType.ERROR,
          "Seleccioná una fecha de pago."
      ).showAndWait();
      return;
    }

    if (view.cmbMetodoPago.getValue() == null) {
      new Alert(
          Alert.AlertType.ERROR,
          "Seleccioná un método de pago."
      ).showAndWait();
      return;
    }

    BigDecimal monto;

    try {
      String raw = view.txtMonto.getText()
          .trim()
          .replace(",", ".");

      monto = new BigDecimal(raw);

    } catch (Exception ex) {
      new Alert(
          Alert.AlertType.ERROR,
          "Monto inválido."
      ).showAndWait();
      return;
    }

    if (monto.compareTo(BigDecimal.ZERO) <= 0) {
      new Alert(
          Alert.AlertType.ERROR,
          "El monto debe ser mayor a 0."
      ).showAndWait();
      return;
    }

    Pago pago = new Pago(
        null,
        fecha,
        monto,
        view.cmbMetodoPago.getValue()
    );

    try {
      view.btnRegistrar.setDisable(true);

      if ("Abonar saldo".equals(view.cmbOperacion.getValue())) {
        if (view.cmbCuota.getValue() == null) {
          throw new IllegalArgumentException(
              "Seleccioná una cuota."
          );
        }

        socioService.abonarSaldo(
            socio.getDni(),
            view.cmbCuota.getValue().getId(),
            pago
        );

      } else {
        socioService.registrarPago(socio.getDni(), pago);
      }

      new Alert(
          Alert.AlertType.INFORMATION,
          "Pago registrado ✅"
      ).showAndWait();

      cerrar();

      if (onRegistrado != null) {
        onRegistrado.run();
      }

    } catch (Exception ex) {
      view.btnRegistrar.setDisable(false);

      new Alert(
          Alert.AlertType.ERROR,
          "Error: " + ex.getMessage()
      ).showAndWait();
    }
  }

  private void cerrar() {
    Stage stage = (Stage) view.getScene().getWindow();
    stage.close();
  }
}