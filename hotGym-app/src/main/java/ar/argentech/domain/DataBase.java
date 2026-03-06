package ar.argentech.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {

  // guarda el archivo en el proyecto (podés moverlo a user.home después)
  private static final String URL = "jdbc:sqlite:hotgym.db";

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL);
  }

}
