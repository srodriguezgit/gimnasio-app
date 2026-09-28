package ar.argentech.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CambioVencimiento(
    LocalDate fechaAnterior,
    LocalDate fechaNueva,
    String motivo,
    String usuario,
    LocalDateTime registrado
) {}
