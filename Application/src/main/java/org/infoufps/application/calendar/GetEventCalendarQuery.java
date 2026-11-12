package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.CalendarFileDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.application.calendar.EventIcsBuilder;
import org.infoufps.application.calendar.PublishedEventLoader;

/**
 * Genera el archivo iCalendar de un evento publicado.
 */
@ApplicationScoped
public class GetEventCalendarQuery {

    private final PublishedEventLoader eventLoader;

    @Inject
    public GetEventCalendarQuery(PublishedEventLoader eventLoader) {
        this.eventLoader = eventLoader;
    }

    /**
     * Ejecuta la generación del .ics.
     *
     * @param eventId identificador del evento.
     * @return archivo iCalendar.
     */
    public CalendarFileDto execute(String eventId) {
        Event event = eventLoader.loadPublished(eventId);
        return EventIcsBuilder.build(event);
    }
}
