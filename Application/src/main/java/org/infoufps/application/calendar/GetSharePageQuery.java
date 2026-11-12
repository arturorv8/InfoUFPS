package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.SharePageDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.application.calendar.EventShareSupport;
import org.infoufps.application.calendar.HtmlEscaper;
import org.infoufps.application.calendar.PublishedEventLoader;
import org.infoufps.application.calendar.CalendarSettings;

/**
 * Genera la página HTML de vista previa para compartir un evento.
 * El título, la descripción y el lugar se escapan porque son escritos por usuarios.
 */
@ApplicationScoped
public class GetSharePageQuery {

    private final PublishedEventLoader eventLoader;
    private final CalendarSettings settings;

    @Inject
    public GetSharePageQuery(PublishedEventLoader eventLoader, CalendarSettings settings) {
        this.eventLoader = eventLoader;
        this.settings = settings;
    }

    /**
     * Ejecuta la generación de la página.
     *
     * @param eventId identificador del evento.
     * @return HTML de vista previa.
     */
    public SharePageDto execute(String eventId) {
        Event event = eventLoader.loadPublished(eventId);

        String eventUrl = HtmlEscaper.escape(EventShareSupport.eventUrl(event, settings));
        String logoUrl = HtmlEscaper.escape(settings.shareLogoUrl());
        String title = HtmlEscaper.escape(event.getTitle());
        String description = HtmlEscaper.escape(EventShareSupport.description(event));

        String html = "<!DOCTYPE html>\n"
                + "<html lang=\"es\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + title + " | InfoUFPS</title>\n"
                + "  <meta property=\"og:type\" content=\"article\">\n"
                + "  <meta property=\"og:title\" content=\"" + title + "\">\n"
                + "  <meta property=\"og:description\" content=\"" + description + "\">\n"
                + "  <meta property=\"og:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta property=\"og:url\" content=\"" + eventUrl + "\">\n"
                + "  <meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "  <meta name=\"twitter:title\" content=\"" + title + "\">\n"
                + "  <meta name=\"twitter:description\" content=\"" + description + "\">\n"
                + "  <meta name=\"twitter:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta http-equiv=\"refresh\" content=\"1;url=" + eventUrl + "\">\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main>\n"
                + "    <img src=\"" + logoUrl + "\" alt=\"InfoUFPS\" width=\"96\" height=\"96\">\n"
                + "    <h1>" + title + "</h1>\n"
                + "    <p>" + description + "</p>\n"
                + "    <p><a href=\"" + eventUrl + "\">Ir al evento</a></p>\n"
                + "  </main>\n"
                + "</body>\n"
                + "</html>\n";

        return new SharePageDto(html);
    }
}
