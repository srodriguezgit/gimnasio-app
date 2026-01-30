package ar.argentech.services.impl;

import ar.argentech.domain.Congelacion;
import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import ar.argentech.services.ISocioService;
import ar.argentech.ui.pagos.PagoDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocioService implements ISocioService {

  private final List<Socio> socios = new ArrayList<>();
  private long nextId = 1;

  @Override
  public List<Socio> obtenerTodos() {
    return new ArrayList<>(socios);
  }

  @Override
  public List<Socio> obtenerMorosos(LocalDate fecha){
    return socios.stream()
        .filter(s -> s.esMoroso(fecha))
        .toList();
  }

  @Override
  public List<Socio> buscar(String texto){
    return socios.stream()
        .filter(s -> s.coincideCon(texto))
        .toList();
  }

  @Override
  public void registrarPago(String dniSocio, Pago pago){
    Socio socio = buscarPorDni(dniSocio)
        .orElseThrow(() ->
            new IllegalArgumentException("Socio no encontrado: DNI " +dniSocio));

    socio.registrarPago(pago);
  }

  private Optional<Socio> buscarPorDni(String dniSocio){
    return socios.stream()
        .filter(s -> s.getDni().equals(dniSocio))
        .findFirst();
  }

  public void agregarSocio(Socio socio) {
    socio.setId(nextId++);
    socios.add(socio);
  }

  public void altaSocioConPago(Socio socio, Pago pago){
    agregarSocio(socio);
    socio.registrarPago(pago);
  }

  public void congelarCuota(String dniSocio, int dias, String motivo, String autorizado){
    if(autorizado == null || autorizado.isBlank()){
      throw new SecurityException("No autorizado.");
    }

    Socio socio = buscarPorDni(dniSocio)
        .orElseThrow(() -> new IllegalArgumentException("Socio no encontrado: DNI " + dniSocio));

    Congelacion cong = new Congelacion(
        null,
        LocalDate.now(),
        dias,
        motivo,
        autorizado,
        LocalDate.now()
    );

    socio.aplicarCongelacion(cong);

  }

  public List<PagoDTO> obtenerPagos() {
    List<PagoDTO> rows = new ArrayList<>();

    for (Socio s : socios) {
      if (s.getPagos() == null) continue;

      for (Pago p : s.getPagos()) {
        if (p == null || p.getFechaPago() == null) continue;

        rows.add(new PagoDTO(
            p.getFechaPago(),
            s.getDni(),
            s.getNombre() + " " + s.getApellido(),
            p.getMonto(),
            p.getMetodoPago()
        ));
      }
    }

    return rows;
  }

  public List<PagoDTO> obtenerPagosEntre(LocalDate desde, LocalDate hasta) {
    return obtenerPagos().stream()
        .filter(r -> (desde == null || !r.getFecha().isBefore(desde)))
        .filter(r -> (hasta == null || !r.getFecha().isAfter(hasta)))
        .toList();
  }

  public boolean eliminarPorId(Long id) {
    return socios.removeIf(s -> s.getId().equals(id));
  }

}
