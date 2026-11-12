package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.shared.exceptions.DomainException;

import java.time.LocalDate;
import java.util.Set;

/**
 * Criterio de consulta para un rango de fechas del calendario.
 * El rango es inclusivo en ambos extremos.
 *
 * @param from  fecha inicial del rango.
 * @param to    fecha final del rango.
 * @param types tipos a filtrar; vacío significa todos.
 */
public record EventRangeCriteria(LocalDate from, LocalDate to, Set<EventType> types) {

    public EventRangeCriteria {
        if (from == null || to == null) {
            throw new DomainException("Las fechas from y to son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new DomainException("La fecha to no puede ser anterior a from");
        }
        types = types == null ? Set.of() : Set.copyOf(types);
    }

    /**
     * @return true si hay filtro de tipos.
     */
    public boolean hasTypes() {
        return !types.isEmpty();
    }
}
