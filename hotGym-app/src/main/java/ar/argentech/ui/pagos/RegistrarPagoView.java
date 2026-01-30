package ar.argentech.ui.pagos;

import ar.argentech.domain.MetodoPago;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class RegistrarPagoView extends BorderPane {

  public Label lblSocio = new Label();

  public DatePicker dpFechaPago = new DatePicker(LocalDate.now());
  public TextField txtMonto = new TextField();
  public ComboBox<MetodoPago> cmbMetodoPago = new ComboBox<>();

  public Button btnRegistrar = new Button("Registrar pago");
  public Button btnCancelar = new Button("Cancelar");

  public RegistrarPagoView() {

    txtMonto.setPromptText("Monto");
    cmbMetodoPago.getItems().addAll(MetodoPago.values());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.setPadding(new Insets(20));

    int row = 0;

    form.add(new Label("Socio:"), 0, row);
    form.add(lblSocio, 1, row++);

    form.add(new Label("Fecha pago:"), 0, row);
    form.add(dpFechaPago, 1, row++);

    form.add(new Label("Monto:"), 0, row);
    form.add(txtMonto, 1, row++);

    form.add(new Label("Método pago:"), 0, row);
    form.add(cmbMetodoPago, 1, row++);

    HBox botones = new HBox(10, btnRegistrar, btnCancelar);
    botones.setAlignment(Pos.CENTER_RIGHT);

    setCenter(form);
    setBottom(botones);
    BorderPane.setMargin(botones, new Insets(10));
  }
}
