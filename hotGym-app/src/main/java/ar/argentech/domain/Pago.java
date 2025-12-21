package ar.argentech.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pago {

  Long id;
  LocalDate fechaPago;
  BigDecimal monto;
  MetodoPago metodoPago;

}
