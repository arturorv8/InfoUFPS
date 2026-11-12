package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.calendar.EventId;

/**
 * Componente reutilizable para cargar un evento en estado PUBLISHED
 * o lanzar NotFoundException en caso de no existir o no estar publicado.
 */
@ApplicationScoped
public class PublishedEventLoader {

    private final IEventRepository eventRepository;

    @Inject
    public PublishedEventLoader(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event loadPublished(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(eventId))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + eventId));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + eventId);
        }
        return event;
    }
}
