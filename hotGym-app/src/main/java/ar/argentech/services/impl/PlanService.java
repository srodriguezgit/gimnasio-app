package ar.argentech.services.impl;

import ar.argentech.domain.DuracionPlan;
import ar.argentech.domain.Plan;
import ar.argentech.services.IPlanService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlanService implements IPlanService {

  private final List<Plan> planes = new ArrayList<>();

  public PlanService() {
    // datos mock iniciales
    planes.add(new Plan(null, "Mensual",
        new BigDecimal("40000"), DuracionPlan.MENSUAL));

    planes.add(new Plan(null, "Quincenal",
        new BigDecimal("25000"), DuracionPlan.QUINCENA));
  }

  @Override
  public List<Plan> obtenerPlanes() {
    return new ArrayList<>(planes);
  }
}