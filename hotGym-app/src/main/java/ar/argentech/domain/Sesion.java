package ar.argentech.domain;

public class Sesion {
  private static Usuario usuarioActual;

  public static Usuario getUsuarioActual() { return usuarioActual; }
  public static void setUsuarioActual(Usuario u) { usuarioActual = u; }

  public static boolean isAdmin() {
    return usuarioActual != null && usuarioActual.getRol() == Rol.ADMIN;
  }
}