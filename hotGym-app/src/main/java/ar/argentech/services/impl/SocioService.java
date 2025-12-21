package ar.argentech.services.impl;

import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import ar.argentech.services.ISocioService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocioService implements ISocioService {

  private final List<Socio> socios = new ArrayList<>();

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
    socios.add(socio);
  }

}
