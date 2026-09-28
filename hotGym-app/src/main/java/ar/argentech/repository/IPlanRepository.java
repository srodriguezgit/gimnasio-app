package ar.argentech.repository;

import ar.argentech.domain.Plan;
import java.util.List;
import java.util.Optional;

public interface IPlanRepository {

  List<Plan> buscarTodos();

  List<Plan> buscarActivos();

  Optional<Plan> buscarPorId(Long id);

  Plan guardar(Plan plan);

  void actualizar(Plan plan);

  void desactivar(Long id);

  void activar(Long id);

}
