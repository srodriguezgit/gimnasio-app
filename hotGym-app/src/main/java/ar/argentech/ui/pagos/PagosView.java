package ar.argentech.ui.pagos;

import ar.argentech.domain.MetodoPago;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;

public class PagosView extends BorderPane {

  public DatePicker dpDesde = new DatePicker();
  public DatePicker dpHasta = new DatePicker();
  public Button btnFiltrar = new Button("Filtrar");
  public Button btnLimpiar = new Button("Limpiar");

  public TableView<PagoDTO> tabla = new TableView<>();

  public Label lblTotal = new Label("Total: $0");
  public Label lblEfectivo = new Label("Efectivo: $0");
  public Label lblTransferencia = new Label("Transferencia: $0");
  public Label lblTarjeta = new Label("Tarjeta: $0");

  public PagosView() {

    HBox filtros = new HBox(10,
        new Label("Desde:"), dpDesde,
        new Label("Hasta:"), dpHasta,
        btnFiltrar, btnLimpiar
    );
    filtros.setPadding(new Insets(10));
    filtros.setAlignment(Pos.CENTER_LEFT);

    HBox totales = new HBox(20, lblTotal, lblEfectivo, lblTransferencia, lblTarjeta);
    totales.setPadding(new Insets(10));
    totales.setAlignment(Pos.CENTER_LEFT);
    totales.setStyle("-fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

    setTop(filtros);
    setCenter(tabla);
    setBottom(totales);
  }
}
