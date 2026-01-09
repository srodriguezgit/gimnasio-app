package ar.argentech.ui.pagos;

import ar.argentech.domain.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagoDTO {
  private LocalDate fecha;
  private String dniSocio;
  private String nombreCompleto;
  private BigDecimal monto;
  private MetodoPago metodo;
}