package ar.argentech.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {

  private static final String URL = "jdbc:sqlite:hotgym.db";

  public static Connection getConnection() throws SQLException {
    Connection connection = DriverManager.getConnection(URL);

    try {
      try (Statement statement = connection.createStatement()) {
        statement.execute("PRAGMA foreign_keys = ON");
      }
      return connection;
    } catch (SQLException e) {
      try {
        connection.close();
      } catch (SQLException closeError) {
        e.addSuppressed(closeError);
      }
      throw e;
    }
  }
}
