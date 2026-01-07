package ar.argentech.ui;

import ar.argentech.domain.MetodoPago;
import ar.argentech.domain.Plan;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javafx.util.StringConverter;

public class NuevoSocioView extends BorderPane {

  public TextField txtNombre = new TextField();
  public TextField txtApellido = new TextField();
  public TextField txtDni = new TextField();
  public ComboBox<Plan> cmbPlan = new ComboBox<>();

  public DatePicker dpFechaPago = new DatePicker();
  public TextField txtMonto = new TextField();
  public ComboBox<MetodoPago> cmbMetodoPago = new ComboBox<>();

  public Button btnRegistrar = new Button("Registrar socio");
  public Button btnCancelar = new Button("Cancelar");

  private static final DateTimeFormatter AR_DATE =
      DateTimeFormatter.ofPattern("d/M/yyyy");

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
    configurarDatePickerAR(dpFechaPago);
  }

  private static void configurarDatePickerAR(DatePicker dp) {

    dp.setPromptText("dd/MM/yyyy");

    dp.setConverter(new StringConverter<>() {
      @Override
      public String toString(LocalDate date) {
        return (date == null) ? "" : AR_DATE.format(date);
      }

      @Override
      public LocalDate fromString(String text) {
        if (text == null || text.isBlank()) return null;
        try {
          return LocalDate.parse(text.trim(), AR_DATE);
        } catch (DateTimeParseException ex) {
          // Si el usuario escribió cualquier cosa, no rompemos: dejamos el valor anterior
          return dp.getValue();
        }
      }
    });

    dp.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
      if (!isFocused) {
        String text = dp.getEditor().getText();
        LocalDate parsedDate = dp.getConverter().fromString(text);
        dp.setValue(parsedDate);
      }
    });
  }

}
