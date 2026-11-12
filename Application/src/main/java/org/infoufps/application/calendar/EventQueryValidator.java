package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validaciones y normalizaciones comunes de las consultas del calendario.
 */
public final class EventQueryValidator {

    /** Cantidad máxima de días que puede abarcar un rango de consulta. */
    public static final int MAX_RANGE_DAYS = 62;

    private EventQueryValidator() {
    }

    /**
     * Valida un rango de consulta y normaliza el filtro de tipos.
     *
     * @param from      fecha inicial.
     * @param to        fecha final.
     * @param rawTypes  valores de tipos recibidos por la API.
     * @return criterio de rango validado.
     */
    public static EventRangeCriteria validateRange(LocalDate from, LocalDate to, List<String> rawTypes) {
        if (from == null || to == null) {
            throw new DomainException("Las fechas from y to son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new DomainException("La fecha to no puede ser anterior a from");
        }
        long inclusiveDays = ChronoUnit.DAYS.between(from, to) + 1;
        if (inclusiveDays > MAX_RANGE_DAYS) {
            throw new DomainException("El rango de consulta no puede superar los " + MAX_RANGE_DAYS + " días");
        }
        return new EventRangeCriteria(from, to, parseTypes(rawTypes));
    }

    /**
     * Convierte los valores recibidos en tipos válidos. Acepta valores repetidos
     * y listas separadas por coma.
     *
     * @param rawTypes valores recibidos.
     * @return conjunto de tipos; vacío significa todos.
     */
    public static Set<EventType> parseTypes(List<String> rawTypes) {
        if (rawTypes == null || rawTypes.isEmpty()) {
            return Set.of();
        }
        EnumSet<EventType> types = EnumSet.noneOf(EventType.class);
        for (String raw : rawTypes) {
            if (raw == null) {
                continue;
            }
            for (String token : raw.split(",")) {
                if (token.isBlank()) {
                    continue;
                }
                types.add(EventType.from(token));
            }
        }
        return types;
    }

    /**
     * Convierte un estado recibido por la API.
     *
     * @param rawStatus valor recibido; si es nulo o vacío usa UNDER_REVIEW.
     * @return estado válido.
     */
    public static Status parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return Status.UNDER_REVIEW;
        }
        try {
            return Status.valueOf(rawStatus.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado inválido: " + rawStatus);
        }
    }
}
