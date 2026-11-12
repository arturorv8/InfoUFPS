package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.calendar.EventId;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Criterio para detectar eventos duplicados.
 * La comparación del título es insensible a mayúsculas/minúsculas.
 *
 * @param title     título del evento.
 * @param startDate fecha de inicio.
 * @param startTime hora de inicio; null para eventos de todo el día.
 * @param excludeId evento que se excluye de la búsqueda (el que se está editando).
 */
public record EventDuplicateCriteria(String title, LocalDate startDate, LocalTime startTime, EventId excludeId) {

    public EventDuplicateCriteria {
        if (title == null || title.isBlank()) {
            throw new DomainException("El título es obligatorio para buscar duplicados");
        }
        if (startDate == null) {
            throw new DomainException("La fecha de inicio es obligatoria para buscar duplicados");
        }
        title = title.strip();
    }
}
