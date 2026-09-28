package ar.argentech.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Cuota {

  private final Long id;
  private final Long socioId;
  private final Long planId;
  private final String nombrePlan;
  private final DuracionPlan duracionPlan;
  private final LocalDate fechaDesde;
  private final LocalDate fechaHasta;
  private final BigDecimal importe;
  private final BigDecimal abonado;

  public BigDecimal getSaldo() {
    return importe.subtract(abonado);
  }

  @Override
  public String toString() {
    return "Cuota #" + id
        + " · " + fechaDesde + " a " + fechaHasta
        + " · saldo $" + getSaldo().toPlainString();
  }
}
