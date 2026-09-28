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
    duracion TEXT NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1
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

      asegurarColumnaActivoEnPlanes(c);
      prepararPersistenciaSocios(c);
      prepararCuotas(c);
      prepararCambiosVencimiento(c);
      crearAdminInicialSiNoExiste(c);
      crearPlanesInicialesSiNoExisten(c);

    } catch (SQLException e) {
      throw new RuntimeException("Error inicializando DB", e);
    }
  }



  private static void crearAdminInicialSiNoExiste(Connection c) throws SQLException {
    boolean hayUsuarios;

    try (Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM usuarios")) {

      rs.next();
      hayUsuarios = rs.getInt("cnt") > 0;
    }

    if (!hayUsuarios) {
      String adminUser = "admin";
      String adminPass = "admin123";
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
  }

  private static void crearPlanesInicialesSiNoExisten(Connection c) throws SQLException {
    boolean hayPlanes;

    try (Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM planes")) {

      rs.next();
      hayPlanes = rs.getInt("cnt") > 0;
    }

    if (!hayPlanes) {
      try (PreparedStatement ps = c.prepareStatement("""
        INSERT INTO planes(nombre, costo, duracion, activo)
        VALUES(?, ?, ?, 1)
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
  }

  private static void asegurarColumnaActivoEnPlanes(Connection c) throws SQLException {
    if (!existeColumna(c, "planes", "activo")) {
      try (Statement st = c.createStatement()) {
        st.executeUpdate("""
          ALTER TABLE planes
          ADD COLUMN activo INTEGER NOT NULL DEFAULT 1
        """);
      }

      System.out.println("Columna activo agregada a planes");
    }
  }

  private static boolean existeColumna(Connection c, String tabla, String columna) throws SQLException {
    try (Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {

      while (rs.next()) {
        String nombreColumna = rs.getString("name");

        if (nombreColumna.equalsIgnoreCase(columna)) {
          return true;
        }
      }

      return false;
    }
  }

  private static void prepararPersistenciaSocios(Connection c)
      throws SQLException {

    String[][] columnas = {
        {"telefono", "TEXT"},
        {"email", "TEXT"},
        {"telefono_familiar", "TEXT"},
        {"activo", "INTEGER NOT NULL DEFAULT 1"}
    };

    for (String[] columna : columnas) {
      if (!existeColumna(c, "socios", columna[0])) {
        try (Statement st = c.createStatement()) {
          st.executeUpdate(
              "ALTER TABLE socios ADD COLUMN "
                  + columna[0] + " " + columna[1]
          );
        }
      }
    }

    try (Statement st = c.createStatement()) {
      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS pagos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                socio_id INTEGER NOT NULL,
                plan_id INTEGER NOT NULL,
                fecha_pago TEXT NOT NULL,
                monto_centavos INTEGER NOT NULL
                    CHECK(monto_centavos > 0),
                metodo_pago TEXT NOT NULL,
                fecha_base_renovacion TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                nombre_plan TEXT NOT NULL,
                duracion_plan TEXT NOT NULL,
                FOREIGN KEY(socio_id) REFERENCES socios(id),
                FOREIGN KEY(plan_id) REFERENCES planes(id)
            )
            """);

      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS congelaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                socio_id INTEGER NOT NULL,
                fecha_desde TEXT NOT NULL,
                dias INTEGER NOT NULL CHECK(dias > 0),
                motivo TEXT NOT NULL,
                autorizado_por TEXT NOT NULL,
                fecha_registro TEXT NOT NULL,
                FOREIGN KEY(socio_id) REFERENCES socios(id)
            )
            """);

      st.executeUpdate("""
            CREATE INDEX IF NOT EXISTS idx_socios_dni
            ON socios(dni)
            """);

      st.executeUpdate("""
            CREATE INDEX IF NOT EXISTS idx_pagos_socio
            ON pagos(socio_id)
            """);

      st.executeUpdate("""
            CREATE INDEX IF NOT EXISTS idx_congelaciones_socio
            ON congelaciones(socio_id)
            """);
    }
  }

  private static void prepararCuotas(Connection c) throws SQLException {
    c.setAutoCommit(false);

    try {
      try (Statement st = c.createStatement()) {
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cuotas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    socio_id INTEGER NOT NULL REFERENCES socios(id),
                    plan_id INTEGER NOT NULL REFERENCES planes(id),
                    nombre_plan TEXT NOT NULL,
                    duracion_plan TEXT NOT NULL,
                    fecha_desde TEXT NOT NULL,
                    fecha_hasta TEXT NOT NULL,
                    importe_centavos INTEGER NOT NULL
                        CHECK(importe_centavos > 0)
                )
                """);
      }

      if (!existeColumna(c, "pagos", "cuota_id")) {
        try (Statement st = c.createStatement()) {
          st.executeUpdate("""
                    ALTER TABLE pagos
                    ADD COLUMN cuota_id INTEGER REFERENCES cuotas(id)
                    """);
        }
      }

      try (Statement st = c.createStatement()) {
        st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_cuotas_socio
                ON cuotas(socio_id)
                """);

        st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_pagos_cuota
                ON pagos(cuota_id)
                """);
      }

      // Los pagos históricos quedan sin cuota asociada.
      // No reconstruimos deudas usando los precios actuales.
      c.commit();

    } catch (SQLException | RuntimeException e) {
      try {
        c.rollback();
      } catch (SQLException rollbackError) {
        e.addSuppressed(rollbackError);
      }

      throw e;

    } finally {
      c.setAutoCommit(true);
    }
  }

  private static void prepararCambiosVencimiento(Connection c)
      throws SQLException {

    try (Statement st = c.createStatement()) {
      st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS cambios_vencimiento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                socio_id INTEGER NOT NULL REFERENCES socios(id),
                fecha_anterior TEXT,
                fecha_nueva TEXT NOT NULL,
                motivo TEXT NOT NULL
                    CHECK(length(trim(motivo)) > 0),
                usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
                username TEXT NOT NULL,
                registrado TEXT NOT NULL
            )
            """);

      st.executeUpdate("""
            CREATE INDEX IF NOT EXISTS idx_cambios_vencimiento_socio
            ON cambios_vencimiento(socio_id)
            """);
    }
  }

}

