package ar.argentech.services.impl;

import ar.argentech.domain.Plan;
import ar.argentech.repository.IPlanRepository;
import ar.argentech.repository.PlanRepository;
import ar.argentech.services.IPlanService;

import java.util.List;
import java.util.Objects;

public class PlanService implements IPlanService {

  private final IPlanRepository planRepository;

  public PlanService() {
    this(new PlanRepository());
  }

  public PlanService(IPlanRepository planRepository) {
    this.planRepository = Objects.requireNonNull(planRepository);
  }

  @Override
  public List<Plan> obtenerPlanes() {
    return planRepository.buscarActivos();
  }
}