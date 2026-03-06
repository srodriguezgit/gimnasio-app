package ar.argentech.repository;

import ar.argentech.domain.PasswordHasher;
import ar.argentech.domain.Usuario;

public class AuthService {

  private final UsuarioRepository repo;

  public AuthService(UsuarioRepository repo) {
    this.repo = repo;
  }

  public Usuario login(String username, String password) {
    if (username == null || username.isBlank() || password == null) return null;

    Usuario u = repo.findByUsername(username.trim());
    if (u == null || !u.isActivo()) return null;

    if (!PasswordHasher.verify(password, u.getPasswordHash())) return null;

    return u;
  }
}
