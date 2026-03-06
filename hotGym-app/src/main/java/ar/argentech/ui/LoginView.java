package ar.argentech.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends BorderPane {

  public TextField txtUser = new TextField();
  public PasswordField txtPass = new PasswordField();
  public Button btnLogin = new Button("Ingresar");
  public Label lblError = new Label();

  public LoginView() {

    // Fondo general
    setStyle("-fx-background-color: #f4f6f8;");

    Label titulo = new Label("Gestión de Gimnasio");
    titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

    Label subtitulo = new Label("Iniciá sesión para continuar");
    subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

    txtUser.setPromptText("Usuario");
    txtPass.setPromptText("Contraseña");

    txtUser.setPrefWidth(260);
    txtPass.setPrefWidth(260);

    txtUser.setStyle("""
        -fx-background-radius: 8;
        -fx-border-radius: 8;
        -fx-padding: 10;
        -fx-border-color: #d1d5db;
        -fx-background-color: white;
        """);

    txtPass.setStyle("""
        -fx-background-radius: 8;
        -fx-border-radius: 8;
        -fx-padding: 10;
        -fx-border-color: #d1d5db;
        -fx-background-color: white;
        """);

    btnLogin.setPrefWidth(260);
    btnLogin.setStyle("""
        -fx-background-color: #2563eb;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
        -fx-padding: 10 16 10 16;
        """);

    lblError.setStyle("-fx-text-fill: #b00020; -fx-font-size: 12px;");

    VBox campos = new VBox(12,
        new Label("Usuario"), txtUser,
        new Label("Contraseña"), txtPass,
        lblError,
        btnLogin
    );
    campos.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(10, titulo, subtitulo, new Separator(), campos);
    card.setAlignment(Pos.CENTER);
    card.setMaxWidth(340);
    card.setPadding(new Insets(30));
    card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 14;
        -fx-border-radius: 14;
        -fx-border-color: #e5e7eb;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.2, 0, 4);
        """);

    StackPane centerWrapper = new StackPane(card);
    centerWrapper.setAlignment(Pos.CENTER);
    centerWrapper.setPadding(new Insets(20));

    setCenter(centerWrapper);
  }
}