package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;

import java.time.LocalDate;
import java.util.Set;

/**
 * Criterio para consultar eventos publicados próximos o en curso.
 *
 * @param types       tipos a filtrar; vacío significa todos.
 * @param today       fecha de referencia en America/Bogota.
 * @param pageRequest paginación solicitada.
 */
public record EventPageCriteria(Set<EventType> types, LocalDate today, PageRequest pageRequest) {

    public EventPageCriteria {
        if (today == null) {
            throw new DomainException("La fecha de referencia es obligatoria");
        }
        types = types == null ? Set.of() : Set.copyOf(types);
        pageRequest = pageRequest != null ? pageRequest : PageRequest.first();
    }

    /**
     * @return true si hay filtro de tipos.
     */
    public boolean hasTypes() {
        return !types.isEmpty();
    }
}
