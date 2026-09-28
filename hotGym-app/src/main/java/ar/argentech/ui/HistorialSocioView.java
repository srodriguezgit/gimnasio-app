package ar.argentech.ui;

import ar.argentech.domain.Contacto;
import ar.argentech.domain.Congelacion;
import ar.argentech.domain.Cuota;
import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ar.argentech.domain.CambioVencimiento;

public class HistorialSocioView extends BorderPane {

  private static final DateTimeFormatter FORMATO_FECHA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public HistorialSocioView(Socio socio, List<Cuota> cuotas) {
    setPadding(new Insets(15));

    Label titulo = new Label(
        socio.getNombre() + " " + socio.getApellido()
            + " · DNI " + texto(socio.getDni())
    );

    setTop(titulo);
    BorderPane.setMargin(titulo, new Insets(0, 0, 15, 0));

    TabPane pestanias = new TabPane();
    pestanias.setTabClosingPolicy(
        TabPane.TabClosingPolicy.UNAVAILABLE
    );

    pestanias.getTabs().addAll(
        new Tab("Resumen", crearResumen(socio, cuotas)),
        new Tab("Cuotas", crearTablaCuotas(cuotas)),
        new Tab("Pagos", crearTablaPagos(socio)),
        new Tab("Congelamientos", crearTablaCongelamientos(socio))
    );

    setCenter(pestanias);

    Button cerrar = new Button("Cerrar");
    cerrar.setOnAction(e -> getScene().getWindow().hide());

    setBottom(cerrar);
    BorderPane.setMargin(cerrar, new Insets(12, 0, 0, 0));
  }

  private ScrollPane crearResumen(Socio socio, List<Cuota> cuotas) {
    BigDecimal saldoPendiente = cuotas.stream()
        .map(Cuota::getSaldo)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<Pago> pagos = socio.getPagos() == null
        ? List.of()
        : socio.getPagos();

    BigDecimal totalAbonado = pagos.stream()
        .map(Pago::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Contacto contacto = socio.getContacto();

    VBox contenido = new VBox(10);
    contenido.setPadding(new Insets(15));

    contenido.getChildren().addAll(
        new Label("Número de socio: " + socio.getId()),
        new Label("Nombre: " + texto(socio.getNombre())),
        new Label("Apellido: " + texto(socio.getApellido())),
        new Label("DNI: " + texto(socio.getDni())),
        new Label(
            "Teléfono: "
                + texto(contacto == null ? null : contacto.getTelefono())
        ),
        new Label(
            "Email: "
                + texto(contacto == null ? null : contacto.getEmail())
        ),
        new Label(
            "Teléfono familiar: "
                + texto(
                contacto == null
                    ? null
                    : contacto.getTelefonoFamiliar()
            )
        ),
        new Separator(),
        new Label(
            "Plan actual: "
                + (
                socio.getPlanActual() == null
                    ? "Sin plan"
                    : socio.getPlanActual().getNombre()
            )
        ),
        new Label("Fecha de alta: " + fecha(socio.getFechaInicio())),
        new Label(
            "Último pago: " + fecha(socio.getFechaUltimoPago())
        ),
        new Label(
            "Vencimiento actual: "
                + fecha(socio.getFechaProximoVencimiento())
        ),
        new Label("Estado de la cuota: " + estado(socio)),
        new Label(
            "Habilitado por cuota hoy: "
                + (
                socio.puedeIngresar(LocalDate.now())
                    ? "Sí"
                    : "No"
            )
        ),
        new Separator(),
        new Label(
            "Saldo pendiente de cuotas registradas: "
                + dinero(saldoPendiente)
        ),
        new Label(
            "Total de pagos registrados: " + dinero(totalAbonado)
        )
    );

    Label aclaracion = new Label(
        "El saldo pendiente corresponde a las cuotas creadas "
            + "con el nuevo sistema. Los pagos históricos se muestran "
            + "en la pestaña Pagos, pero no permiten determinar "
            + "si existían deudas anteriores."
    );

    aclaracion.setWrapText(true);
    contenido.getChildren().add(aclaracion);

    ScrollPane scroll = new ScrollPane(contenido);
    scroll.setFitToWidth(true);

    return scroll;
  }

  private TableView<Cuota> crearTablaCuotas(List<Cuota> cuotas) {
    TableView<Cuota> tabla = new TableView<>(
        FXCollections.observableArrayList(cuotas)
    );

    tabla.setPlaceholder(
        new Label("Este socio todavía no tiene cuotas registradas.")
    );

    tabla.getColumns().addAll(
        columna("N.º", q -> String.valueOf(q.getId()), 65),
        columna("Plan contratado", Cuota::getNombrePlan, 140),
        columna("Desde", q -> fecha(q.getFechaDesde()), 100),
        columna("Hasta", q -> fecha(q.getFechaHasta()), 100),
        columna("Importe", q -> dinero(q.getImporte()), 115),
        columna("Abonado", q -> dinero(q.getAbonado()), 115),
        columna("Saldo", q -> dinero(q.getSaldo()), 115),
        columna(
            "Pago de cuota",
            q -> q.getSaldo().signum() > 0
                ? "Saldo pendiente"
                : "Saldada",
            130
        )
    );

    return tabla;
  }

  private TableView<Pago> crearTablaPagos(Socio socio) {
    List<Pago> pagos = socio.getPagos() == null
        ? List.of()
        : socio.getPagos().stream()
        .sorted(
            Comparator.comparing(
                Pago::getFechaPago,
                Comparator.nullsLast(
                    Comparator.<LocalDate>reverseOrder()
                )
            )
        )
        .toList();

    TableView<Pago> tabla = new TableView<>(
        FXCollections.observableArrayList(pagos)
    );

    tabla.setPlaceholder(
        new Label("Este socio no tiene pagos registrados.")
    );

    tabla.getColumns().addAll(
        columna("N.º de pago", p -> String.valueOf(p.getId()), 110),
        columna("Fecha", p -> fecha(p.getFechaPago()), 140),
        columna("Monto", p -> dinero(p.getMonto()), 160),
        columna(
            "Método",
            p -> p.getMetodoPago() == null
                ? "—"
                : p.getMetodoPago().name(),
            180
        )
    );

    return tabla;
  }

  private TableView<Congelacion> crearTablaCongelamientos(Socio socio) {
    List<Congelacion> congelaciones =
        socio.getCongelaciones() == null
            ? List.of()
            : socio.getCongelaciones();

    TableView<Congelacion> tabla = new TableView<>(
        FXCollections.observableArrayList(congelaciones)
    );

    tabla.setPlaceholder(
        new Label("Este socio no tiene congelamientos registrados.")
    );

    tabla.getColumns().addAll(
        columna("Desde", c -> fecha(c.getFechaDesde()), 100),
        columna("Regreso", c -> fecha(c.getFechaHasta()), 100),
        columna("Días", c -> String.valueOf(c.getDias()), 65),
        columna("Motivo", Congelacion::getMotivo, 250),
        columna("Autorizado por", Congelacion::getAutorizadoPor, 160),
        columna("Registrado", c -> fecha(c.getFechaRegistro()), 110)
    );

    return tabla;
  }

  private static <T> TableColumn<T, String> columna(
      String titulo,
      Function<T, String> obtenerValor,
      double ancho
  ) {
    TableColumn<T, String> columna = new TableColumn<>(titulo);

    columna.setCellValueFactory(
        celda -> new ReadOnlyStringWrapper(
            obtenerValor.apply(celda.getValue())
        )
    );

    columna.setPrefWidth(ancho);

    // Las fechas e importes se muestran formateados como texto.
    // Evitamos que JavaFX los ordene alfabéticamente.
    columna.setSortable(false);

    return columna;
  }

  private static String estado(Socio socio) {
    LocalDate hoy = LocalDate.now();

    if (socio.getFechaProximoVencimiento() == null) {
      return "Sin período registrado";
    }

    if (socio.esMoroso(hoy)) {
      return "Vencida";
    }

    if (socio.estaCongelado(hoy)) {
      return "Congelada";
    }

    if (socio.getFechaInicio() != null
        && hoy.isBefore(socio.getFechaInicio())) {
      return "Todavía no inició";
    }

    return "Vigente";
  }

  private static String fecha(LocalDate fecha) {
    return fecha == null ? "—" : fecha.format(FORMATO_FECHA);
  }

  private static String dinero(BigDecimal importe) {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("es-AR")
    ).format(importe);
  }

  private static String texto(String valor) {
    return valor == null || valor.isBlank() ? "—" : valor;
  }

  public void agregarCambiosVencimiento(
      List<CambioVencimiento> cambios
  ) {
    TableView<CambioVencimiento> tabla = new TableView<>(
        FXCollections.observableArrayList(cambios)
    );

    tabla.setPlaceholder(
        new Label("Este socio no tiene ajustes de vencimiento.")
    );

    DateTimeFormatter formato =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    tabla.getColumns().addAll(
        columna(
            "Registrado",
            c -> c.registrado().format(formato),
            170
        ),
        columna(
            "Fecha anterior",
            c -> fecha(c.fechaAnterior()),
            120
        ),
        columna(
            "Fecha nueva",
            c -> fecha(c.fechaNueva()),
            120
        ),
        columna("Motivo", CambioVencimiento::motivo, 300),
        columna("Usuario", CambioVencimiento::usuario, 140)
    );

    TabPane pestanias = (TabPane) getCenter();

    pestanias.getTabs().add(
        new Tab("Ajustes de vencimiento", tabla)
    );
  }

}
