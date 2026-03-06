package ar.argentech.repository;

import ar.argentech.domain.DataBase;
import ar.argentech.domain.Rol;
import ar.argentech.domain.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioRepository {

  public Usuario findByUsername(String username) {
    try (Connection c = DataBase.getConnection();
         PreparedStatement ps = c.prepareStatement("""
           SELECT id, username, password_hash, rol, activo
           FROM usuarios
           WHERE username = ?
         """)) {
      ps.setString(1, username);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;

        return new Usuario(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            Rol.valueOf(rs.getString("rol")),
            rs.getInt("activo") == 1
        );
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error buscando usuario", e);
    }
  }
}