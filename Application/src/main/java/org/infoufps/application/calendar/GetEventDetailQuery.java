package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.PublicEventDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.PublishedEventLoader;

/**
 * Consulta el detalle público de un evento publicado.
 */
@ApplicationScoped
public class GetEventDetailQuery {

    private final PublishedEventLoader eventLoader;
    private final EventDtoAssembler assembler;

    @Inject
    public GetEventDetailQuery(PublishedEventLoader eventLoader, EventDtoAssembler assembler) {
        this.eventLoader = eventLoader;
        this.assembler = assembler;
    }

    /**
     * Obtiene el detalle de un evento publicado.
     *
     * @param eventId identificador del evento.
     * @return detalle del evento.
     */
    public PublicEventDto execute(String eventId) {
        Event event = eventLoader.loadPublished(eventId);
        return assembler.toPublicDto(event);
    }
}
