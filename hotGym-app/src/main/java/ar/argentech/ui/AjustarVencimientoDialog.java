package ar.argentech.ui;

import ar.argentech.domain.Socio;
import ar.argentech.services.impl.SocioService;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class AjustarVencimientoDialog {

  public static void mostrar(
      Socio socio,
      SocioService service,
      Window owner,
      Runnable refrescar
  ) {
    Dialog<Void> dialogo = new Dialog<>();
    dialogo.initOwner(owner);
    dialogo.setTitle("Modificar vencimiento");

    dialogo.setHeaderText(
        socio.getNombre() + " " + socio.getApellido()
    );

    DatePicker fecha = new DatePicker(
        socio.getFechaProximoVencimiento()
    );

    // Se elige desde el calendario para evitar fechas mal escritas.
    fecha.setEditable(false);

    TextArea motivo = new TextArea();
    motivo.setPromptText("Motivo del ajuste");
    motivo.setPrefRowCount(3);
    motivo.setWrapText(true);

    Label aviso = new Label(
        "El ajuste no modifica pagos ni saldos. "
            + "Las próximas renovaciones partirán de esta fecha."
    );

    aviso.setWrapText(true);

    Label error = new Label();
    error.setWrapText(true);

    VBox contenido = new VBox(
        10,
        new Label(
            "Vencimiento actual: "
                + (
                socio.getFechaProximoVencimiento() == null
                    ? "Sin fecha"
                    : socio.getFechaProximoVencimiento()
            )
        ),
        new Label("Nuevo vencimiento:"),
        fecha,
        new Label("Motivo:"),
        motivo,
        aviso,
        error
    );

    dialogo.getDialogPane().setContent(contenido);
    dialogo.getDialogPane().setPrefWidth(460);

    ButtonType guardar = new ButtonType(
        "Guardar ajuste",
        ButtonBar.ButtonData.OK_DONE
    );

    dialogo.getDialogPane().getButtonTypes().addAll(
        guardar,
        ButtonType.CANCEL
    );

    Button boton = (Button) dialogo.getDialogPane()
        .lookupButton(guardar);

    boton.addEventFilter(ActionEvent.ACTION, evento -> {
      evento.consume();
      boton.setDisable(true);

      try {
        service.modificarVencimiento(
            socio.getId(),
            socio.getFechaProximoVencimiento(),
            fecha.getValue(),
            motivo.getText()
        );

      } catch (RuntimeException ex) {
        error.setText(ex.getMessage());
        boton.setDisable(false);
        return;
      }

      dialogo.close();

      if (refrescar != null) {
        refrescar.run();
      }
    });

    dialogo.showAndWait();
  }
}
