package ar.argentech.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Congelacion {
  private Long id;
  private LocalDate fechaDesde;
  private int dias;
  private String motivo;
  private String autorizadoPor; // por ahora string simple luego Usuario
  private LocalDate fechaRegistro;

  public LocalDate getFechaHasta() {
    return fechaDesde.plusDays(dias);
  }
}
