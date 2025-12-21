package ar.argentech.ui;

import ar.argentech.services.IPlanService;
import ar.argentech.services.impl.PlanService;
import ar.argentech.services.impl.SocioService;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class MainController {

  private final MainView mainView;
  private final SocioService socioService;
  private final PlanService planService;

  private SocioController socioController;

  public MainController(MainView mainView, SocioService socioService, PlanService planService) {
    this.mainView = mainView;
    this.socioService = socioService;
    this.planService = new PlanService();

    configurarMenu();
    mostrarHome();
  }

  private void configurarMenu() {

    Menu menuSocios = new Menu("Socios");
    MenuItem itemVerTodos = new MenuItem("Ver todos");
    MenuItem itemMorosos = new MenuItem("Morosos");
    MenuItem itemNuevoSocio = new MenuItem("Nuevo socio");

    menuSocios.getItems().addAll(itemVerTodos, itemMorosos, itemNuevoSocio);
    mainView.getMenuBar().getMenus().add(menuSocios);

    itemVerTodos.setOnAction(e -> mostrarSocios());
    itemMorosos.setOnAction(e -> mostrarMorosos());
    itemNuevoSocio.setOnAction(e -> {
      NuevoSocioView view = new NuevoSocioView();
      new NuevoSocioController(view, socioService, planService);
      mainView.setContenido(view);
    });
  }

  private void mostrarHome() {
    mainView.setContenido(new HomeView());
  }

  private void mostrarSocios() {
    SociosView sociosView = new SociosView();
    socioController = new SocioController(sociosView, socioService);
    mainView.setContenido(sociosView);
  }

  private void mostrarMorosos() {
    if (socioController == null) {
      mostrarSocios();
    }
    socioController.mostrarMorosos();
  }
}