package ar.argentech.repository;

import ar.argentech.domain.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocioRepository {

  @FunctionalInterface
  public interface Operacion<T> {
    T ejecutar(Connection connection) throws SQLException;
  }

  public <T> T enTransaccion(Operacion<T> operacion) {
    try (Connection c = DataBase.getConnection()) {
      c.setAutoCommit(false);

      try {
        T resultado = operacion.ejecutar(c);
        c.commit();
        return resultado;
      } catch (SQLException | RuntimeException e) {
        try {
          c.rollback();
        } catch (SQLException rollbackError) {
          e.addSuppressed(rollbackError);
        }
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException(
          "No se pudo completar la operación en la base de datos.", e
      );
    }
  }

  public List<Socio> buscarTodos() {
    return enTransaccion(c -> {
      List<Socio> socios = new ArrayList<>();

      try (PreparedStatement ps = c.prepareStatement(
          consultaSocios()
              + " WHERE s.activo = 1 ORDER BY s.apellido, s.nombre");
           ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
          socios.add(mapearSocio(rs));
        }
      }

      for (Socio socio : socios) {
        cargarHistorial(c, socio);
      }

      return socios;
    });
  }

  public Optional<Socio> buscarPorDni(Connection c, String dni)
      throws SQLException {

    Socio socio = null;

    try (PreparedStatement ps = c.prepareStatement(
        consultaSocios() + " WHERE s.activo = 1 AND s.dni = ?")) {

      ps.setString(1, dni);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          socio = mapearSocio(rs);
        }

        if (rs.next()) {
          throw new IllegalStateException(
              "Hay más de un socio activo con ese DNI."
          );
        }
      }
    }

    if (socio != null) {
      cargarHistorial(c, socio);
    }

    return Optional.ofNullable(socio);
  }

  public long insertar(Connection c, Socio socio) throws SQLException {
    String sql = """
            INSERT INTO socios (
                nombre, apellido, dni,
                telefono, email, telefono_familiar,
                plan_id, fecha_inicio, fecha_ultimo_pago,
                fecha_proximo_vencimiento, activo
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
            """;

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, socio.getNombre().trim());
      ps.setString(2, socio.getApellido().trim());
      ps.setString(3, socio.getDni().trim());

      Contacto contacto = socio.getContacto();

      ps.setString(4, contacto == null ? null : contacto.getTelefono());
      ps.setString(5, contacto == null ? null : contacto.getEmail());
      ps.setString(
          6, contacto == null ? null : contacto.getTelefonoFamiliar()
      );

      if (socio.getPlanActual() == null) {
        ps.setNull(7, Types.INTEGER);
      } else {
        ps.setLong(7, socio.getPlanActual().getId());
      }

      ps.setString(8, textoFecha(socio.getFechaInicio()));
      ps.setString(9, textoFecha(socio.getFechaUltimoPago()));
      ps.setString(10, textoFecha(socio.getFechaProximoVencimiento()));

      ps.executeUpdate();
    }

    // Se consulta en la misma conexión que realizó el INSERT.
    try (Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
      rs.next();
      return rs.getLong(1);
    }
  }

  public void actualizarFechas(Connection c, Socio socio)
      throws SQLException {

    try (PreparedStatement ps = c.prepareStatement("""
            UPDATE socios
            SET fecha_ultimo_pago = ?,
                fecha_proximo_vencimiento = ?
            WHERE id = ? AND activo = 1
            """)) {

      ps.setString(1, textoFecha(socio.getFechaUltimoPago()));
      ps.setString(2, textoFecha(socio.getFechaProximoVencimiento()));
      ps.setLong(3, socio.getId());

      if (ps.executeUpdate() != 1) {
        throw new IllegalStateException(
            "No se pudo actualizar el socio."
        );
      }
    }
  }

  public void insertarPago(
      Connection c,
      Socio socio,
      Pago pago,
      LocalDate fechaBase,
      LocalDate nuevoVencimiento
  ) throws SQLException {

    long centavos = pago.getMonto()
        .setScale(2, RoundingMode.UNNECESSARY)
        .movePointRight(2)
        .longValueExact();

    try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO pagos (
                socio_id, plan_id, fecha_pago, monto_centavos,
                metodo_pago, fecha_base_renovacion, fecha_vencimiento,
                nombre_plan, duracion_plan
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

      ps.setLong(1, socio.getId());
      ps.setLong(2, socio.getPlanActual().getId());
      ps.setString(3, pago.getFechaPago().toString());
      ps.setLong(4, centavos);
      ps.setString(5, pago.getMetodoPago().name());
      ps.setString(6, fechaBase.toString());
      ps.setString(7, nuevoVencimiento.toString());

      // Conservamos qué se contrató aunque luego se edite el plan.
      ps.setString(8, socio.getPlanActual().getNombre());
      ps.setString(
          9, socio.getPlanActual().getDuracionPlan().name()
      );

      ps.executeUpdate();
    }
  }

  public void insertarCongelacion(
      Connection c, Long socioId, Congelacion congelacion
  ) throws SQLException {

    try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO congelaciones (
                socio_id, fecha_desde, dias, motivo,
                autorizado_por, fecha_registro
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """)) {

      ps.setLong(1, socioId);
      ps.setString(2, congelacion.getFechaDesde().toString());
      ps.setInt(3, congelacion.getDias());
      ps.setString(4, congelacion.getMotivo());
      ps.setString(5, congelacion.getAutorizadoPor());
      ps.setString(6, congelacion.getFechaRegistro().toString());

      ps.executeUpdate();
    }
  }

  public boolean desactivar(Connection c, Long id) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("""
            UPDATE socios SET activo = 0
            WHERE id = ? AND activo = 1
            """)) {

      ps.setLong(1, id);
      return ps.executeUpdate() == 1;
    }
  }

  private String consultaSocios() {
    return """
            SELECT
                s.id, s.nombre, s.apellido, s.dni,
                s.telefono, s.email, s.telefono_familiar,
                s.fecha_inicio, s.fecha_ultimo_pago,
                s.fecha_proximo_vencimiento,
                p.id AS p_id,
                p.nombre AS p_nombre,
                p.costo AS p_costo,
                p.duracion AS p_duracion,
                p.activo AS p_activo
            FROM socios s
            LEFT JOIN planes p ON p.id = s.plan_id
            """;
  }

  private Socio mapearSocio(ResultSet rs) throws SQLException {
    Plan plan = null;

    if (rs.getObject("p_id") != null) {
      plan = new Plan(
          rs.getLong("p_id"),
          rs.getString("p_nombre"),
          rs.getBigDecimal("p_costo"),
          DuracionPlan.valueOf(rs.getString("p_duracion")),
          rs.getInt("p_activo") == 1
      );
    }

    Contacto contacto = new Contacto(
        rs.getString("telefono"),
        rs.getString("email"),
        rs.getString("telefono_familiar")
    );

    return new Socio(
        rs.getLong("id"),
        rs.getString("nombre"),
        rs.getString("apellido"),
        rs.getString("dni"),
        contacto,
        plan,
        fecha(rs.getString("fecha_inicio")),
        fecha(rs.getString("fecha_ultimo_pago")),
        fecha(rs.getString("fecha_proximo_vencimiento")),
        new ArrayList<>(),
        new ArrayList<>()
    );
  }

  private void cargarHistorial(Connection c, Socio socio)
      throws SQLException {

    try (PreparedStatement ps = c.prepareStatement("""
            SELECT id, fecha_pago, monto_centavos, metodo_pago
            FROM pagos
            WHERE socio_id = ?
            ORDER BY fecha_pago, id
            """)) {

      ps.setLong(1, socio.getId());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          socio.getPagos().add(new Pago(
              rs.getLong("id"),
              fecha(rs.getString("fecha_pago")),
              BigDecimal.valueOf(
                  rs.getLong("monto_centavos"), 2
              ),
              MetodoPago.valueOf(rs.getString("metodo_pago"))
          ));
        }
      }
    }

    try (PreparedStatement ps = c.prepareStatement("""
            SELECT id, fecha_desde, dias, motivo,
                   autorizado_por, fecha_registro
            FROM congelaciones
            WHERE socio_id = ?
            ORDER BY fecha_desde, id
            """)) {

      ps.setLong(1, socio.getId());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          socio.getCongelaciones().add(new Congelacion(
              rs.getLong("id"),
              fecha(rs.getString("fecha_desde")),
              rs.getInt("dias"),
              rs.getString("motivo"),
              rs.getString("autorizado_por"),
              fecha(rs.getString("fecha_registro"))
          ));
        }
      }
    }
  }

  private static String textoFecha(LocalDate fecha) {
    return fecha == null ? null : fecha.toString();
  }

  private static LocalDate fecha(String texto) {
    return texto == null || texto.isBlank()
        ? null
        : LocalDate.parse(texto);
  }
}
