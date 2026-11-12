package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.EventDuplicateCriteria;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.calendar.EventId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Guardia de duplicados del calendario.
 * <p>
 * Un evento se considera duplicado si coincide en título (sin distinguir
 * mayúsculas/minúsculas), fecha de inicio y hora de inicio. Si el duplicado ya
 * está publicado se rechaza la operación; si está en revisión se permite y se
 * marca para el administrador.
 */
@ApplicationScoped
public class EventDuplicateGuard {

    private final IEventRepository eventRepository;

    @Inject
    public EventDuplicateGuard(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Rechaza la operación si existe un evento publicado con los mismos datos.
     *
     * @param title     título.
     * @param startDate fecha de inicio.
     * @param startTime hora de inicio; null para todo el día.
     * @param excludeId evento que se excluye (edición).
     */
    public void ensureNoPublishedDuplicate(String title, LocalDate startDate, LocalTime startTime, EventId excludeId) {
        List<Event> duplicates = eventRepository.findDuplicates(
                new EventDuplicateCriteria(title, startDate, startTime, excludeId));
        duplicates.stream()
                .filter(event -> event.getStatus() == Status.PUBLISHED)
                .findFirst()
                .ifPresent(event -> {
                    throw new ConflictException(
                            "Ya existe un evento publicado con el mismo título, fecha y hora: "
                                    + event.getId().value());
                });
    }

    /**
     * Obtiene los posibles duplicados de un evento.
     *
     * @param event evento de referencia.
     * @return eventos candidatos.
     */
    public List<Event> findPossibleDuplicates(Event event) {
        return eventRepository.findDuplicates(new EventDuplicateCriteria(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId()));
    }
}
