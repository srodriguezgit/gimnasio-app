package ar.argentech.repository;


import ar.argentech.domain.DataBase;
import ar.argentech.domain.DuracionPlan;
import ar.argentech.domain.Plan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlanRepository implements IPlanRepository {

  @Override
  public List<Plan> buscarTodos() {
    String sql = "SELECT id, nombre, costo, duracion, activo FROM planes ORDER BY nombre";

    List<Plan> planes = new ArrayList<>();

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        planes.add(mapearPlan(resultSet));
      }

      return planes;

    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar planes", e);
    }
  }

  @Override
  public List<Plan> buscarActivos() {
    String sql = """
                SELECT id, nombre, costo, duracion, activo
                FROM planes
                WHERE activo = true
                ORDER BY nombre
                """;

    List<Plan> planes = new ArrayList<>();

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        planes.add(mapearPlan(resultSet));
      }

      return planes;

    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar planes activos", e);
    }
  }

  @Override
  public Optional<Plan> buscarPorId(Long id) {
    String sql = """
                SELECT id, nombre, costo, duracion, activo
                FROM planes
                WHERE id = ?
                """;

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setLong(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(mapearPlan(resultSet));
        }
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar plan por ID", e);
    }
  }

  @Override
  public Plan guardar(Plan plan) {
    String sql = """
                INSERT INTO planes (nombre, costo, duracion, activo)
                VALUES (?, ?, ?, ?)
                """;

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, plan.getNombre());
      statement.setBigDecimal(2, plan.getCosto());
      statement.setString(3, plan.getDuracionPlan().name());
      statement.setBoolean(4, plan.getActivo());

      statement.executeUpdate();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          Long idGenerado = generatedKeys.getLong(1);

          return new Plan(
              idGenerado,
              plan.getNombre(),
              plan.getCosto(),
              plan.getDuracionPlan(),
              plan.getActivo()
          );
        }
      }

      throw new RuntimeException("No se pudo obtener el ID generado del plan");

    } catch (SQLException e) {
      throw new RuntimeException("Error al guardar plan", e);
    }
  }

  @Override
  public void actualizar(Plan plan) {
    String sql = """
                UPDATE planes
                SET nombre = ?, costo = ?, duracion = ?, activo = ?
                WHERE id = ?
                """;

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, plan.getNombre());
      statement.setBigDecimal(2, plan.getCosto());
      statement.setString(3, plan.getDuracionPlan().name());
      statement.setBoolean(4, plan.getActivo());
      statement.setLong(5, plan.getId());

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar plan", e);
    }
  }

  @Override
  public void desactivar(Long id) {
    cambiarEstado(id, false);
  }

  @Override
  public void activar(Long id) {
    cambiarEstado(id, true);
  }

  private void cambiarEstado(Long id, boolean activo) {
    String sql = "UPDATE planes SET activo = ? WHERE id = ?";

    try (Connection connection = DataBase.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setBoolean(1, activo);
      statement.setLong(2, id);

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al cambiar estado del plan", e);
    }
  }

  private Plan mapearPlan(ResultSet resultSet) throws SQLException {
    return new Plan(
        resultSet.getLong("id"),
        resultSet.getString("nombre"),
        resultSet.getBigDecimal("costo"),
        DuracionPlan.valueOf(resultSet.getString("duracion")),
        resultSet.getBoolean("activo")
    );
  }
}
