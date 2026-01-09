package ar.argentech.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class CongelarCuotaView extends BorderPane {

  public Spinner<Integer> spDias = new Spinner<>(1, 365, 7);
  public TextField txtMotivo = new TextField();
  public TextField txtAutorizadoPor = new TextField(); // por ahora simple
  public Button btnConfirmar = new Button("Confirmar");
  public Button btnCancelar = new Button("Cancelar");

  public CongelarCuotaView() {
    txtMotivo.setPromptText("Ej: Vacaciones / Lesión");
    txtAutorizadoPor.setPromptText("Usuario/Encargado");

    spDias.setEditable(true);

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.setPadding(new Insets(20));

    int row = 0;
    form.add(new Label("Días a congelar:"), 0, row);
    form.add(spDias, 1, row++);

    form.add(new Label("Motivo:"), 0, row);
    form.add(txtMotivo, 1, row++);

    form.add(new Label("Autorizado por:"), 0, row);
    form.add(txtAutorizadoPor, 1, row++);

    HBox botones = new HBox(10, btnConfirmar, btnCancelar);
    botones.setAlignment(Pos.CENTER_RIGHT);

    setCenter(form);
    setBottom(botones);
    BorderPane.setMargin(botones, new Insets(10));
  }

}

