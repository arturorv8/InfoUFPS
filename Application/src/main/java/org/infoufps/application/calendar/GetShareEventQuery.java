package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.ShareDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.application.calendar.EventShareSupport;
import org.infoufps.application.calendar.GoogleCalendarLinkBuilder;
import org.infoufps.application.calendar.PublishedEventLoader;
import org.infoufps.application.calendar.CalendarSettings;

/**
 * Obtiene los datos para compartir un evento publicado.
 */
@ApplicationScoped
public class GetShareEventQuery {

    private final PublishedEventLoader eventLoader;
    private final CalendarSettings settings;

    @Inject
    public GetShareEventQuery(PublishedEventLoader eventLoader, CalendarSettings settings) {
        this.eventLoader = eventLoader;
        this.settings = settings;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param eventId identificador del evento.
     * @return texto y enlaces para compartir.
     */
    public ShareDto execute(String eventId) {
        Event event = eventLoader.loadPublished(eventId);
        return new ShareDto(
                EventShareSupport.message(event, settings),
                EventShareSupport.eventUrl(event, settings),
                GoogleCalendarLinkBuilder.build(event),
                EventShareSupport.icsUrl(event, settings)
        );
    }
}
