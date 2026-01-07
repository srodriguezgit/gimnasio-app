package ar.argentech.ui;

import ar.argentech.domain.Plan;
import ar.argentech.domain.Socio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class EditarSocioView extends BorderPane {

  public TextField txtNombre = new TextField();
  public TextField txtApellido = new TextField();
  public TextField txtDni = new TextField();
  public ComboBox<Plan> cmbPlan = new ComboBox<>();

  public Button btnGuardar = new Button("Guardar");
  public Button btnCancelar = new Button("Cancelar");

  public EditarSocioView() {

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

    HBox botones = new HBox(10, btnGuardar, btnCancelar);
    botones.setAlignment(Pos.CENTER_RIGHT);

    setCenter(form);
    setBottom(botones);
    BorderPane.setMargin(botones, new Insets(10));
  }

  public void cargarSocio(Socio socio) {
    txtNombre.setText(socio.getNombre());
    txtApellido.setText(socio.getApellido());
    txtDni.setText(socio.getDni());
    cmbPlan.setValue(socio.getPlanActual());
  }
}
