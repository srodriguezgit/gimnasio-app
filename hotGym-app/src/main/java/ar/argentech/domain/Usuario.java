package ar.argentech.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Usuario {
  private Long id;
  private String username;
  private String passwordHash;
  private Rol rol;
  private boolean activo;
}
