package ar.argentech.ui;

import ar.argentech.domain.Socio;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

  private final MenuBar menuBar = new MenuBar();
  private final VBox leftMenu = new VBox(10);

  public MainView() {
    setTop(menuBar);
    setLeft(leftMenu);
  }

  public MenuBar getMenuBar() {
    return menuBar;
  }

  public VBox getLeftMenu() {
    return leftMenu;
  }

  public void setContenido(Node node) {
    setCenter(node);
  }
}