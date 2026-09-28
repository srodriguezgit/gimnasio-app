package ar.argentech.repository;

import ar.argentech.domain.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CuotaRepository {

  public static long centavos(BigDecimal monto) {
    if (monto == null || monto.signum() <= 0) {
      throw new IllegalArgumentException(
          "El importe debe ser mayor a cero."
      );
    }

    try {
      return monto
          .setScale(2, RoundingMode.UNNECESSARY)
          .movePointRight(2)
          .longValueExact();
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(
          "Importe inválido: usá como máximo dos decimales."
      );
    }
  }

  public List<Cuota> buscar(Connection c, long socioId)
      throws SQLException {

    List<Cuota> cuotas = new ArrayList<>();

    try (PreparedStatement ps = c.prepareStatement("""
            SELECT q.*, COALESCE(SUM(p.monto_centavos), 0) AS abonado
            FROM cuotas q
            LEFT JOIN pagos p ON p.cuota_id = q.id
            WHERE q.socio_id = ?
            GROUP BY q.id
            ORDER BY q.id DESC
            """)) {

      ps.setLong(1, socioId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          cuotas.add(new Cuota(
              rs.getLong("id"),
              rs.getLong("socio_id"),
              rs.getLong("plan_id"),
              rs.getString("nombre_plan"),
              DuracionPlan.valueOf(
                  rs.getString("duracion_plan")
              ),
              LocalDate.parse(rs.getString("fecha_desde")),
              LocalDate.parse(rs.getString("fecha_hasta")),
              BigDecimal.valueOf(
                  rs.getLong("importe_centavos"), 2
              ),
              BigDecimal.valueOf(rs.getLong("abonado"), 2)
          ));
        }
      }
    }

    return cuotas;
  }

  public Cuota crear(
      Connection c,
      Socio socio,
      LocalDate desde,
      LocalDate hasta
  ) throws SQLException {

    Plan plan = socio.getPlanActual();
    long importe = centavos(plan.getCosto());

    try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO cuotas (
                socio_id, plan_id, nombre_plan, duracion_plan,
                fecha_desde, fecha_hasta, importe_centavos
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """)) {

      ps.setLong(1, socio.getId());
      ps.setLong(2, plan.getId());
      ps.setString(3, plan.getNombre());
      ps.setString(4, plan.getDuracionPlan().name());
      ps.setString(5, desde.toString());
      ps.setString(6, hasta.toString());
      ps.setLong(7, importe);

      ps.executeUpdate();
    }

    try (Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(
             "SELECT last_insert_rowid()"
         )) {

      rs.next();

      return new Cuota(
          rs.getLong(1),
          socio.getId(),
          plan.getId(),
          plan.getNombre(),
          plan.getDuracionPlan(),
          desde,
          hasta,
          BigDecimal.valueOf(importe, 2),
          BigDecimal.ZERO
      );
    }
  }

  public void abonar(Connection c, Cuota cuota, Pago pago)
      throws SQLException {

    if (pago.getMonto().compareTo(cuota.getSaldo()) > 0) {
      throw new IllegalArgumentException(
          "El pago supera el saldo pendiente de la cuota."
      );
    }

    try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO pagos (
                socio_id, plan_id, fecha_pago, monto_centavos,
                metodo_pago, fecha_base_renovacion,
                fecha_vencimiento, nombre_plan,
                duracion_plan, cuota_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

      ps.setLong(1, cuota.getSocioId());
      ps.setLong(2, cuota.getPlanId());
      ps.setString(3, pago.getFechaPago().toString());
      ps.setLong(4, centavos(pago.getMonto()));
      ps.setString(5, pago.getMetodoPago().name());
      ps.setString(6, cuota.getFechaDesde().toString());
      ps.setString(7, cuota.getFechaHasta().toString());
      ps.setString(8, cuota.getNombrePlan());
      ps.setString(9, cuota.getDuracionPlan().name());
      ps.setLong(10, cuota.getId());

      ps.executeUpdate();
    }
  }
}
