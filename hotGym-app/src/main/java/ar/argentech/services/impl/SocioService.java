package ar.argentech.services.impl;

import ar.argentech.domain.Congelacion;
import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import ar.argentech.repository.SocioRepository;
import ar.argentech.services.ISocioService;
import ar.argentech.ui.pagos.PagoDTO;
import ar.argentech.domain.Cuota;
import ar.argentech.repository.CuotaRepository;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ar.argentech.domain.Sesion;
import ar.argentech.domain.Usuario;
import ar.argentech.domain.Rol;
import ar.argentech.domain.CambioVencimiento;
import java.time.LocalDateTime;
import java.util.Objects;

public class SocioService implements ISocioService {

  private final SocioRepository repository;
  private final CuotaRepository cuotas = new CuotaRepository();

  public SocioService() {
    this.repository = new SocioRepository();
  }

  @Override
  public List<Socio> obtenerTodos() {
    return repository.buscarTodos();
  }

  @Override
  public List<Socio> obtenerMorosos(LocalDate fecha) {
    if (fecha == null) {
      throw new IllegalArgumentException("La fecha es obligatoria.");
    }

    return obtenerTodos().stream()
        .filter(s -> s.esMoroso(fecha))
        .toList();
  }

  @Override
  public List<Socio> buscar(String texto) {
    return obtenerTodos().stream()
        .filter(s -> s.coincideCon(texto))
        .toList();
  }

  @Override
  public void registrarPago(String dniSocio, Pago pago) {
    validarPago(pago);

    repository.enTransaccion(c -> {
      Socio socio = exigirSocio(c, dniSocio);

      registrarPagoEnTransaccion(c, socio, pago);
      return null;
    });
  }

  public void agregarSocio(Socio socio) {
    validarAlta(socio);

    long id = repository.enTransaccion(c -> {
      verificarDniDisponible(c, socio.getDni());
      return repository.insertar(c, socio);
    });

    // Solo se modifica el objeto recibido después del commit.
    socio.setId(id);
  }

  public void altaSocioConPago(Socio socio, Pago pago) {
    validarAlta(socio);
    validarPago(pago);

    // Trabajamos sobre una copia para no modificar la pantalla
    // si el guardado falla.
    Socio nuevo = new Socio(
        null,
        socio.getNombre(),
        socio.getApellido(),
        socio.getDni(),
        socio.getContacto(),
        socio.getPlanActual(),
        socio.getFechaInicio(),
        null,
        null,
        new ArrayList<>(),
        new ArrayList<>()
    );

    repository.enTransaccion(c -> {
      verificarDniDisponible(c, nuevo.getDni());

      nuevo.setId(repository.insertar(c, nuevo));
      registrarPagoEnTransaccion(c, nuevo, pago);
      return null;
    });

    socio.setId(nuevo.getId());
    socio.setFechaUltimoPago(nuevo.getFechaUltimoPago());
    socio.setFechaProximoVencimiento(
        nuevo.getFechaProximoVencimiento()
    );
    socio.setPagos(new ArrayList<>(nuevo.getPagos()));
  }

  private void registrarPagoEnTransaccion(
      Connection c,
      Socio socio,
      Pago pago
  ) throws SQLException {

    if (socio.getPlanActual() == null
        || socio.getPlanActual().getId() == null) {
      throw new IllegalArgumentException(
          "El socio debe tener un plan guardado en la base."
      );
    }

    LocalDate fechaBase = socio.getFechaProximoVencimiento() != null
        ? socio.getFechaProximoVencimiento()
        : pago.getFechaPago();

    // Esta operación representa una renovación.
    socio.registrarPago(pago);

    Cuota cuota = cuotas.crear(
        c,
        socio,
        fechaBase,
        socio.getFechaProximoVencimiento()
    );

    cuotas.abonar(c, cuota, pago);

    repository.actualizarFechas(c, socio);
  }

  public List<Cuota> obtenerCuotas(String dniSocio) {
    return repository.enTransaccion(c ->
        cuotas.buscar(c, exigirSocio(c, dniSocio).getId())
    );
  }

  public void abonarSaldo(String dniSocio, Long cuotaId, Pago pago) {
    validarPago(pago);

    if (cuotaId == null) {
      throw new IllegalArgumentException("Seleccioná una cuota.");
    }

    repository.enTransaccion(c -> {
      Socio socio = exigirSocio(c, dniSocio);

      Cuota cuota = cuotas.buscar(c, socio.getId()).stream()
          .filter(q -> q.getId().equals(cuotaId))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              "La cuota no pertenece al socio."
          ));

      cuotas.abonar(c, cuota, pago);

      if (socio.getFechaUltimoPago() == null
          || pago.getFechaPago().isAfter(
          socio.getFechaUltimoPago()
      )) {
        socio.setFechaUltimoPago(pago.getFechaPago());
      }

      // No llamamos a socio.registrarPago():
      // completar el saldo no debe renovar el período.
      repository.actualizarFechas(c, socio);

      return null;
    });
  }

  public void congelarCuota(
      String dniSocio,
      int dias,
      String motivo,
      String autorizado
  ) {
    /*
     * Esta comprobación conserva la firma actual.
     * NO sustituye la validación de un usuario administrador.
     */
    if (autorizado == null || autorizado.isBlank()) {
      throw new SecurityException("No autorizado.");
    }

    if (dias <= 0) {
      throw new IllegalArgumentException(
          "Los días deben ser mayores a cero."
      );
    }

    if (motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException(
          "Ingresá el motivo del congelamiento."
      );
    }

    repository.enTransaccion(c -> {
      Socio socio = exigirSocio(c, dniSocio);
      LocalDate desde = LocalDate.now();

      if (socio.getFechaProximoVencimiento() == null
          || socio.esMoroso(desde)) {
        throw new IllegalArgumentException(
            "El socio debe tener una cuota vigente para congelarla."
        );
      }

      LocalDate hasta = desde.plusDays(dias);

      boolean superpuesta = socio.getCongelaciones().stream()
          .anyMatch(existente ->
              desde.isBefore(existente.getFechaHasta())
                  && existente.getFechaDesde().isBefore(hasta)
          );

      if (superpuesta) {
        throw new IllegalArgumentException(
            "Ya existe un congelamiento para parte de ese período."
        );
      }

      Congelacion congelacion = new Congelacion(
          null,
          desde,
          dias,
          motivo.trim(),
          autorizado.trim(),
          desde
      );

      socio.aplicarCongelacion(congelacion);

      repository.insertarCongelacion(
          c, socio.getId(), congelacion
      );

      repository.actualizarFechas(c, socio);
      return null;
    });
  }

  public List<PagoDTO> obtenerPagos() {
    // Incluye pagos de socios dados de baja.
    return repository.enTransaccion(c -> {
      List<PagoDTO> pagos = new ArrayList<>();

      try (PreparedStatement ps = c.prepareStatement("""
                SELECT p.fecha_pago, p.monto_centavos, p.metodo_pago,
                       s.dni, s.nombre, s.apellido
                FROM pagos p
                JOIN socios s ON s.id = p.socio_id
                ORDER BY p.fecha_pago DESC, p.id DESC
                """);
           ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
          pagos.add(new PagoDTO(
              LocalDate.parse(rs.getString("fecha_pago")),
              rs.getString("dni"),
              rs.getString("nombre") + " "
                  + rs.getString("apellido"),
              java.math.BigDecimal.valueOf(
                  rs.getLong("monto_centavos"), 2
              ),
              ar.argentech.domain.MetodoPago.valueOf(
                  rs.getString("metodo_pago")
              )
          ));
        }
      }

      return pagos;
    });
  }

  public List<PagoDTO> obtenerPagosEntre(
      LocalDate desde, LocalDate hasta
  ) {
    if (desde != null && hasta != null && desde.isAfter(hasta)) {
      throw new IllegalArgumentException(
          "La fecha desde no puede ser posterior a la fecha hasta."
      );
    }

    return obtenerPagos().stream()
        .filter(p -> desde == null || !p.getFecha().isBefore(desde))
        .filter(p -> hasta == null || !p.getFecha().isAfter(hasta))
        .toList();
  }

  public boolean eliminarPorId(Long id) {
    if (id == null) {
      throw new IllegalArgumentException(
          "El identificador del socio es obligatorio."
      );
    }

    return repository.enTransaccion(
        c -> repository.desactivar(c, id)
    );
  }

  private Socio exigirSocio(Connection c, String dni)
      throws SQLException {

    if (dni == null || dni.isBlank()) {
      throw new IllegalArgumentException("El DNI es obligatorio.");
    }

    return repository.buscarPorDni(c, dni.trim())
        .orElseThrow(() -> new IllegalArgumentException(
            "Socio activo no encontrado: DNI " + dni
        ));
  }

  private void verificarDniDisponible(Connection c, String dni)
      throws SQLException {

    // También detecta socios dados de baja: no creamos otra ficha.
    try (PreparedStatement ps = c.prepareStatement(
        "SELECT id FROM socios WHERE TRIM(dni) = ?")) {

      ps.setString(1, dni.trim());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          throw new IllegalArgumentException(
              "Ya existe un socio con ese DNI. "
                  + "Si está dado de baja, debe reactivarse."
          );
        }
      }
    }
  }

  private void validarAlta(Socio socio) {
    if (socio == null) {
      throw new IllegalArgumentException("El socio es obligatorio.");
    }

    if (socio.getId() != null) {
      throw new IllegalArgumentException(
          "El socio ya tiene un identificador."
      );
    }

    if (socio.getNombre() == null || socio.getNombre().isBlank()
        || socio.getApellido() == null
        || socio.getApellido().isBlank()) {
      throw new IllegalArgumentException(
          "Nombre y apellido son obligatorios."
      );
    }

    if (socio.getDni() == null || socio.getDni().isBlank()) {
      throw new IllegalArgumentException("El DNI es obligatorio.");
    }

    if (socio.getPlanActual() != null
        && socio.getPlanActual().getId() == null) {
      throw new IllegalArgumentException(
          "Seleccioná un plan guardado en la base."
      );
    }

    if (socio.getPagos() != null && !socio.getPagos().isEmpty()
        || socio.getCongelaciones() != null
        && !socio.getCongelaciones().isEmpty()) {
      throw new IllegalArgumentException(
          "El alta no admite un historial precargado."
      );
    }
  }

  private void validarPago(Pago pago) {
    if (pago == null
        || pago.getFechaPago() == null
        || pago.getMetodoPago() == null) {
      throw new IllegalArgumentException(
          "Completá la fecha y el método de pago."
      );
    }

    if (pago.getId() != null) {
      throw new IllegalArgumentException(
          "Este pago ya tiene un identificador."
      );
    }

    if (pago.getMonto() == null || pago.getMonto().signum() <= 0) {
      throw new IllegalArgumentException(
          "El monto debe ser mayor a cero."
      );
    }

    try {
      pago.getMonto()
          .setScale(2, RoundingMode.UNNECESSARY)
          .movePointRight(2)
          .longValueExact();
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(
          "El monto debe tener como máximo dos decimales "
              + "y estar dentro del rango admitido."
      );
    }
  }

  private Usuario exigirUsuario(
      Connection c,
      boolean requiereAdmin
  ) throws SQLException {

    Usuario sesion = Sesion.getUsuarioActual();

    if (sesion == null || sesion.getId() == null) {
      throw new SecurityException(
          "Iniciá sesión para realizar esta operación."
      );
    }

    try (PreparedStatement ps = c.prepareStatement("""
        SELECT username, rol, activo
        FROM usuarios
        WHERE id = ?
        """)) {

      ps.setLong(1, sesion.getId());

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next() || rs.getInt("activo") != 1) {
          throw new SecurityException(
              "El usuario no está activo."
          );
        }

        Rol rol = Rol.valueOf(rs.getString("rol"));

        if (requiereAdmin && rol != Rol.ADMIN) {
          throw new SecurityException(
              "Solo un administrador puede modificar el vencimiento."
          );
        }

        return new Usuario(
            sesion.getId(),
            rs.getString("username"),
            null,
            rol,
            true
        );
      }
    }
  }

  public void actualizarDatosSocio(
      Long id,
      String nombre,
      String apellido,
      String dni,
      Long planId
  ) {
    if (id == null
        || nombre == null || nombre.isBlank()
        || apellido == null || apellido.isBlank()
        || dni == null || dni.isBlank()
        || planId == null) {

      throw new IllegalArgumentException(
          "Completá nombre, apellido, DNI y plan."
      );
    }

    repository.enTransaccion(c -> {
      exigirUsuario(c, false);

      Long planAnterior;

      try (PreparedStatement ps = c.prepareStatement("""
            SELECT plan_id
            FROM socios
            WHERE id = ? AND activo = 1
            """)) {

        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            throw new IllegalArgumentException(
                "Socio activo no encontrado."
            );
          }

          planAnterior = rs.getObject("plan_id") == null
              ? null
              : rs.getLong("plan_id");
        }
      }

      try (PreparedStatement ps = c.prepareStatement("""
            SELECT id
            FROM socios
            WHERE TRIM(dni) = ? AND id <> ?
            """)) {

        ps.setString(1, dni.trim());
        ps.setLong(2, id);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            throw new IllegalArgumentException(
                "El DNI ya pertenece a otro socio, "
                    + "incluso si está dado de baja."
            );
          }
        }
      }

      try (PreparedStatement ps = c.prepareStatement("""
            SELECT activo FROM planes WHERE id = ?
            """)) {

        ps.setLong(1, planId);

        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()
              || (
              rs.getInt("activo") != 1
                  && !Objects.equals(planAnterior, planId)
          )) {

            throw new IllegalArgumentException(
                "Seleccioná un plan activo."
            );
          }
        }
      }

      try (PreparedStatement ps = c.prepareStatement("""
            UPDATE socios
            SET nombre = ?, apellido = ?, dni = ?, plan_id = ?
            WHERE id = ? AND activo = 1
            """)) {

        ps.setString(1, nombre.trim());
        ps.setString(2, apellido.trim());
        ps.setString(3, dni.trim());
        ps.setLong(4, planId);
        ps.setLong(5, id);

        if (ps.executeUpdate() != 1) {
          throw new IllegalStateException(
              "No se pudo guardar el socio."
          );
        }
      }

      // Cambiar de plan afecta futuras renovaciones.
      // No modifica cuotas ni pagos ya registrados.
      return null;
    });
  }

  public void modificarVencimiento(
      Long socioId,
      LocalDate fechaEsperada,
      LocalDate nueva,
      String motivo
  ) {
    if (socioId == null
        || nueva == null
        || motivo == null
        || motivo.isBlank()) {

      throw new IllegalArgumentException(
          "Seleccioná una fecha e ingresá el motivo."
      );
    }

    repository.enTransaccion(c -> {
      Usuario usuario = exigirUsuario(c, true);

      LocalDate anterior;

      try (PreparedStatement ps = c.prepareStatement("""
            SELECT fecha_proximo_vencimiento
            FROM socios
            WHERE id = ? AND activo = 1
            """)) {

        ps.setLong(1, socioId);

        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            throw new IllegalArgumentException(
                "Socio activo no encontrado."
            );
          }

          String fecha = rs.getString(
              "fecha_proximo_vencimiento"
          );

          anterior = fecha == null
              ? null
              : LocalDate.parse(fecha);
        }
      }

      if (!Objects.equals(anterior, fechaEsperada)) {
        throw new IllegalStateException(
            "El vencimiento cambió. Cerrá esta ventana "
                + "y actualizá el listado."
        );
      }

      if (Objects.equals(anterior, nueva)) {
        throw new IllegalArgumentException(
            "La fecha nueva debe ser diferente de la actual."
        );
      }

      try (PreparedStatement ps = c.prepareStatement("""
            UPDATE socios
            SET fecha_proximo_vencimiento = ?
            WHERE id = ? AND activo = 1
            """)) {

        ps.setString(1, nueva.toString());
        ps.setLong(2, socioId);

        if (ps.executeUpdate() != 1) {
          throw new IllegalStateException(
              "No se pudo actualizar el vencimiento."
          );
        }
      }

      try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO cambios_vencimiento (
                socio_id, fecha_anterior, fecha_nueva,
                motivo, usuario_id, username, registrado
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """)) {

        ps.setLong(1, socioId);
        ps.setString(
            2, anterior == null ? null : anterior.toString()
        );
        ps.setString(3, nueva.toString());
        ps.setString(4, motivo.trim());
        ps.setLong(5, usuario.getId());
        ps.setString(6, usuario.getUsername());
        ps.setString(7, LocalDateTime.now().toString());

        ps.executeUpdate();
      }

      return null;
    });
  }

  public List<CambioVencimiento> obtenerCambiosVencimiento(
      Long socioId
  ) {
    if (socioId == null) {
      throw new IllegalArgumentException("Seleccioná un socio.");
    }

    return repository.enTransaccion(c -> {
      exigirUsuario(c, false);

      List<CambioVencimiento> cambios = new ArrayList<>();

      try (PreparedStatement ps = c.prepareStatement("""
            SELECT *
            FROM cambios_vencimiento
            WHERE socio_id = ?
            ORDER BY id DESC
            """)) {

        ps.setLong(1, socioId);

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            String anterior = rs.getString("fecha_anterior");

            cambios.add(new CambioVencimiento(
                anterior == null
                    ? null
                    : LocalDate.parse(anterior),
                LocalDate.parse(rs.getString("fecha_nueva")),
                rs.getString("motivo"),
                rs.getString("username"),
                LocalDateTime.parse(rs.getString("registrado"))
            ));
          }
        }
      }

      return cambios;
    });
  }

}