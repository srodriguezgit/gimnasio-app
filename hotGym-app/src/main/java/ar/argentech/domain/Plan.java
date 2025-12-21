package ar.argentech.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Plan {

  Long id;
  String nombre;
  BigDecimal costo;
  DuracionPlan duracionPlan;

}
