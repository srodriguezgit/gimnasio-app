package ar.argentech.ui.pagos;

import ar.argentech.domain.MetodoPago;
import ar.argentech.services.impl.SocioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

public class PagosController {

  private final PagosView view;
  private final SocioService socioService;

  private final ObservableList<PagoDTO> pagosObs = FXCollections.observableArrayList();
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public PagosController(PagosView view, SocioService socioService) {
    this.view = view;
    this.socioService = socioService;

    configurarTabla();
    cargarTodos();
    configurarEventos();
  }

  private void configurarTabla() {

    TableColumn<PagoDTO, String> colFecha = new TableColumn<>("Fecha");
    colFecha.setCellValueFactory(c -> {
      LocalDate f = c.getValue().getFecha();
      return new ReadOnlyStringWrapper(f == null ? "" : f.format(fmt));
    });

    TableColumn<PagoDTO, String> colDni = new TableColumn<>("DNI");
    colDni.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDniSocio()));

    TableColumn<PagoDTO, String> colNombre = new TableColumn<>("Socio");
    colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombreCompleto()));

    TableColumn<PagoDTO, String> colMetodo = new TableColumn<>("Método");
    colMetodo.setCellValueFactory(c -> new ReadOnlyStringWrapper(
        c.getValue().getMetodo() == null ? "" : c.getValue().getMetodo().name()
    ));

    TableColumn<PagoDTO, String> colMonto = new TableColumn<>("Monto");
    colMonto.setCellValueFactory(c -> new ReadOnlyStringWrapper(
        c.getValue().getMonto() == null ? "" : c.getValue().getMonto().toString()
    ));

    view.tabla.getColumns().setAll(colFecha, colDni, colNombre, colMetodo, colMonto);
    view.tabla.setItems(pagosObs);
  }

  private void configurarEventos() {
    view.btnFiltrar.setOnAction(e -> filtrar());
    view.btnLimpiar.setOnAction(e -> {
      view.dpDesde.setValue(null);
      view.dpHasta.setValue(null);
      cargarTodos();
    });
  }

  private void cargarTodos() {
    pagosObs.setAll(socioService.obtenerPagos());
    recalcularTotales();
  }

  private void filtrar() {
    LocalDate desde = view.dpDesde.getValue();
    LocalDate hasta = view.dpHasta.getValue();

    // opcional: validar rango
    if (desde != null && hasta != null && hasta.isBefore(desde)) {
      // intercambiamos o podrías mostrar alert
      LocalDate tmp = desde; desde = hasta; hasta = tmp;
    }

    pagosObs.setAll(socioService.obtenerPagosEntre(desde, hasta));
    recalcularTotales();
  }

  private void recalcularTotales() {
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal efectivo = BigDecimal.ZERO;
    BigDecimal transf = BigDecimal.ZERO;
    BigDecimal tarjeta = BigDecimal.ZERO;

    for (PagoDTO r : pagosObs) {
      BigDecimal m = r.getMonto();
      if (m == null) continue;

      total = total.add(m);

      MetodoPago metodo = r.getMetodo();
      if (metodo == null) continue;

      switch (metodo) {
        case EFECTIVO -> efectivo = efectivo.add(m);
        case TRANSFERENCIA -> transf = transf.add(m);
        case TARJETA -> tarjeta = tarjeta.add(m);
      }
    }

    view.lblTotal.setText("Total: $" + total);
    view.lblEfectivo.setText("Efectivo: $" + efectivo);
    view.lblTransferencia.setText("Transferencia: $" + transf);
    view.lblTarjeta.setText("Tarjeta: $" + tarjeta);
  }
}
