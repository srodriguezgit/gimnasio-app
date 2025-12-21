package ar.argentech.ui;

import ar.argentech.domain.MetodoPago;
import ar.argentech.domain.Plan;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class NuevoSocioView extends BorderPane {

  public TextField txtNombre = new TextField();
  public TextField txtApellido = new TextField();
  public TextField txtDni = new TextField();
  public ComboBox<Plan> cmbPlan = new ComboBox<>();

  public DatePicker dpFechaPago = new DatePicker(LocalDate.now());
  public TextField txtMonto = new TextField();
  public ComboBox<MetodoPago> cmbMetodoPago = new ComboBox<>();

  public Button btnRegistrar = new Button("Registrar socio");
  public Button btnCancelar = new Button("Cancelar");

  public NuevoSocioView() {

    txtNombre.setPromptText("Nombre");
    txtApellido.setPromptText("Apellido");
    txtDni.setPromptText("DNI");

    cmbMetodoPago.getItems().addAll(MetodoPago.values());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.setPadding(new Insets(20));

    int row = 0;
    form.add(new Label("Nombre:"), 0, row);
    form.add(txtNombre, 1, row++);

    form.add(new Label("Apellido:"), 0, row);
    form.add(txtApellido, 1, row++);

    form.add(new Label("DNI:"), 0, row);
    form.add(txtDni, 1, row++);

    form.add(new Label("Plan:"), 0, row);
    form.add(cmbPlan, 1, row++);

    form.add(new Separator(), 0, row++, 2, 1);

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
