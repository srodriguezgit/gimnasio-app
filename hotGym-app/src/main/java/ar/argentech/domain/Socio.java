package ar.argentech.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Socio {
  private Long id;
  private String nombre;
  private String apellido;
  private String dni;
  private Contacto contacto;
  private Plan planActual;

  private LocalDate fechaInicio;
  private LocalDate fechaUltimoPago;
  private LocalDate fechaProximoVencimiento;

  private List<Pago> pagos = new ArrayList<>();
  private List< Congelacion> congelaciones = new ArrayList<>();

  public boolean esMoroso(LocalDate fecha){
    return fechaProximoVencimiento !=null && fechaProximoVencimiento.isBefore(fecha);
  }

  public boolean esMoroso(){
    return esMoroso(LocalDate.now());
  }

  public void registrarPago(Pago pago) {
    if (pago == null) {
      throw new IllegalArgumentException("El pago es obligatorio.");
    }
    if (pago.getFechaPago() == null) {
      throw new IllegalArgumentException("La fecha de pago es obligatoria.");
    }
    if (pago.getMonto() == null || pago.getMonto().signum() <= 0) {
      throw new IllegalArgumentException("El monto debe ser mayor a cero.");
    }
    if (pago.getMetodoPago() == null) {
      throw new IllegalArgumentException("El método de pago es obligatorio.");
    }
    if (planActual == null || planActual.getDuracionPlan() == null) {
      throw new IllegalStateException(
          "El socio debe tener un plan con duración definida."
      );
    }

    LocalDate fechaBase = fechaProximoVencimiento != null
        ? fechaProximoVencimiento
        : pago.getFechaPago();

    LocalDate nuevoVencimiento =
        planActual.getDuracionPlan().calcularVencimiento(fechaBase);

    if (pagos == null) {
      pagos = new ArrayList<>();
    }

    pagos.add(pago);
    fechaProximoVencimiento = nuevoVencimiento;

    // Cargar un cobro antiguo no debe retroceder la fecha del último pago.
    if (fechaUltimoPago == null
        || pago.getFechaPago().isAfter(fechaUltimoPago)) {
      fechaUltimoPago = pago.getFechaPago();
    }
  }

  public boolean coincideCon(String texto){

    if (texto == null || texto.isBlank()) {
      return true; // no filtra nada
    }

    String[] palabras = texto.toLowerCase().trim().split("\\s+");

    String dniNorm = dni == null
        ? "" : dni.toLowerCase(java.util.Locale.ROOT);

    String nombreNorm = nombre == null
        ? "" : nombre.toLowerCase(java.util.Locale.ROOT);

    String apellidoNorm = apellido == null
        ? "" : apellido.toLowerCase(java.util.Locale.ROOT);

    for (String palabra : palabras) {
      boolean coincide =
          dniNorm.contains(palabra)
              || nombreNorm.contains(palabra)
              || apellidoNorm.contains(palabra);

      if(!coincide){
        return false;
      }
    }
    return true;
  }

  public void aplicarCongelacion(Congelacion congelacion){

    if(congelacion==null) throw new IllegalArgumentException("No se puede aplicar congelacion: MOTIVO NULL");
    if(congelacion.getDias() <=0) throw new IllegalArgumentException("Los dias deben ser mayores a 0(cero)");


    if(congelaciones==null) congelaciones = new ArrayList<>();
    congelaciones.add(congelacion);

    if(fechaProximoVencimiento!=null){
      fechaProximoVencimiento = fechaProximoVencimiento.plusDays(congelacion.getDias());
    }

  }

  public boolean estaCongelado(LocalDate fecha) {
    if (fecha == null) {
      throw new IllegalArgumentException("La fecha es obligatoria.");
    }

    if (congelaciones == null) {
      return false;
    }

    return congelaciones.stream().anyMatch(congelacion ->
        !fecha.isBefore(congelacion.getFechaDesde())
            && fecha.isBefore(congelacion.getFechaHasta())
    );
  }

  public boolean tieneCuotaVigente(LocalDate fecha) {
    if (fecha == null) {
      throw new IllegalArgumentException("La fecha es obligatoria.");
    }

    return fechaProximoVencimiento != null
        && !fechaProximoVencimiento.isBefore(fecha)
        && (fechaInicio == null || !fecha.isBefore(fechaInicio));
  }

  public boolean puedeIngresar(LocalDate fecha) {
    return tieneCuotaVigente(fecha) && !estaCongelado(fecha);
  }

}
