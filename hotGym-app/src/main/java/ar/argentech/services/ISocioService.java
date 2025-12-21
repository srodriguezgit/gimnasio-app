package ar.argentech.services;

import ar.argentech.domain.Pago;
import ar.argentech.domain.Socio;
import java.time.LocalDate;
import java.util.List;

public interface ISocioService {

  List<Socio> obtenerTodos();

  List<Socio> obtenerMorosos(LocalDate fecha);

  List<Socio> buscar(String texto);

  void registrarPago(String dniSocio, Pago pago);

}
