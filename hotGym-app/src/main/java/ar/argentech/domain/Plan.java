package ar.argentech.domain;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Plan {

  Long id;
  String nombre;
  BigDecimal costo;
  DuracionPlan duracionPlan;
  Boolean activo;

  public Plan(Long id, String nombre, BigDecimal costo, DuracionPlan duracionPlan, Boolean activo) {
    validarNombre(nombre);
    validarCosto(costo);
    validarDuracion(duracionPlan);

    this.id = id;
    this.nombre = nombre;
    this.costo = costo;
    this.duracionPlan = duracionPlan;
    this.activo = activo;
  }

  public void modificarNombre(String nuevoNombre) {
    validarNombre(nuevoNombre);
    this.nombre = nuevoNombre;
  }

  public void modificarCosto(BigDecimal nuevoCosto) {
    validarCosto(nuevoCosto);
    this.costo = nuevoCosto;
  }

  public void modificarDuracion(DuracionPlan nuevaDuracion) {
    validarDuracion(nuevaDuracion);
    this.duracionPlan = nuevaDuracion;
  }

  public void activar() {
    this.activo = true;
  }

  public void desactivar() {
    this.activo = false;
  }

  private void validarCosto(BigDecimal costo) {
    if (costo == null || costo.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("El nombre del plan no puede ser nulo ni negativo.");
    }
  }

  private void validarDuracion(DuracionPlan duracionPlan) {
    if (duracionPlan == null) {
      throw new IllegalArgumentException("La duracion del plan no puede estar vacio.");
    }
  }

  private void validarNombre(String nombre) {
    if(nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre del plan no puede estar vacio.");
    }
  }

}
