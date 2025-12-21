package ar.argentech.domain;

import java.time.LocalDate;

public enum DuracionPlan {

  MENSUAL{
    @Override
    public LocalDate calcularVencimiento(LocalDate fechaBase){
      return fechaBase.plusMonths(1);
    }
  },
  SEMANAL{
    @Override
    public LocalDate calcularVencimiento(LocalDate fechaBase){
      return fechaBase.plusDays(7);
    }
  },
  QUINCENA{
    @Override
    public LocalDate calcularVencimiento(LocalDate fechaBase){
      return fechaBase.plusDays(15);
    }
  },
  TRIMESTRAL{
    @Override
    public LocalDate calcularVencimiento(LocalDate fechaBase){
      return fechaBase.plusMonths(3);
    }
  };

  public abstract LocalDate calcularVencimiento(LocalDate fechaBase);

}
