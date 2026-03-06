package ar.argentech;

import ar.argentech.domain.DataBase;
import ar.argentech.domain.PasswordHasher;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbInit {

  public static void init() {
    try (Connection c = DataBase.getConnection()) {

      try (Statement st = c.createStatement()) {
        st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            rol TEXT NOT NULL,
            activo INTEGER NOT NULL DEFAULT 1
          );
        """);
        st.executeUpdate("""
  CREATE TABLE IF NOT EXISTS planes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    costo REAL NOT NULL,
    duracion TEXT NOT NULL
  );
""");
        st.executeUpdate("""
  CREATE TABLE IF NOT EXISTS socios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    dni TEXT,
    plan_id INTEGER,
    fecha_inicio TEXT,
    fecha_ultimo_pago TEXT,
    fecha_proximo_vencimiento TEXT,
    FOREIGN KEY(plan_id) REFERENCES planes(id)
  );
""");
        st.executeUpdate("""
  CREATE TABLE IF NOT EXISTS socios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    dni TEXT,
    plan_id INTEGER,
    fecha_inicio TEXT,
    fecha_ultimo_pago TEXT,
    fecha_proximo_vencimiento TEXT,
    FOREIGN KEY(plan_id) REFERENCES planes(id)
  );
""");

      }

      // Si no hay usuarios, crea admin por defecto
      boolean hayUsuarios;
      try (Statement st = c.createStatement();
           ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM usuarios")) {
        rs.next();
        hayUsuarios = rs.getInt("cnt") > 0;
      }

      if (!hayUsuarios) {
        String adminUser = "admin";
        String adminPass = "admin123"; // después lo cambiás
        String hash = PasswordHasher.hash(adminPass);

        try (PreparedStatement ps = c.prepareStatement("""
          INSERT INTO usuarios(username, password_hash, rol, activo)
          VALUES(?, ?, 'ADMIN', 1)
        """)) {
          ps.setString(1, adminUser);
          ps.setString(2, hash);
          ps.executeUpdate();
        }

        System.out.println("Admin creado: user=admin pass=admin123 (CAMBIAR ASAP)");
      }

      boolean hayPlanes;

      try (Statement st = c.createStatement();
           ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM planes")) {
        rs.next();
        hayPlanes = rs.getInt("cnt") > 0;
      }

      if (!hayPlanes) {

        try (PreparedStatement ps = c.prepareStatement("""
    INSERT INTO planes(nombre, costo, duracion)
    VALUES(?, ?, ?)
  """)) {

          ps.setString(1, "Mensual");
          ps.setBigDecimal(2, new BigDecimal("43000"));
          ps.setString(3, "MENSUAL");
          ps.executeUpdate();

          ps.setString(1, "Semanal");
          ps.setBigDecimal(2, new BigDecimal("15000"));
          ps.setString(3, "SEMANAL");
          ps.executeUpdate();

          ps.setString(1, "Quincena");
          ps.setBigDecimal(2, new BigDecimal("33000"));
          ps.setString(3, "QUINCENA");
          ps.executeUpdate();

          ps.setString(1, "Trimestral");
          ps.setBigDecimal(2, new BigDecimal("108000"));
          ps.setString(3, "TRIMESTRAL");
          ps.executeUpdate();
        }

        System.out.println("Planes iniciales creados");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error inicializando DB", e);
    }
  }
}
