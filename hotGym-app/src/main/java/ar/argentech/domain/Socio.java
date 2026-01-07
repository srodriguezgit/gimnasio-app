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

  public boolean esMoroso(LocalDate fecha){
    return fechaProximoVencimiento !=null && fechaProximoVencimiento.isBefore(fecha);
  }

  public boolean esMoroso(){
    return esMoroso(LocalDate.now());
  }

  public void registrarPago(Pago pago){

    if (pagos == null) pagos = new ArrayList<>();

    System.out.println("fechaPago = " + pago.getFechaPago());

    pagos.add(pago);
    fechaUltimoPago = pago.getFechaPago();
    System.out.println("fechaUltimoPago = " + fechaUltimoPago);

    fechaProximoVencimiento = planActual.getDuracionPlan().calcularVencimiento(fechaUltimoPago);
    System.out.println("fechaProximoVencimiento = " + fechaProximoVencimiento);
  }

  public boolean coincideCon(String texto){

    if (texto == null || texto.isBlank()) {
      return true; // no filtra nada
    }

    String[] palabras = texto.toLowerCase().trim().split("\\s+");

    String dniNorm = dni.toLowerCase();
    String nombreNorm = nombre.toLowerCase();
    String apellidoNorm = apellido.toLowerCase();

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

}
