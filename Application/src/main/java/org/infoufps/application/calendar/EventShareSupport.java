package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.application.calendar.CalendarSettings;

import java.time.format.DateTimeFormatter;

/**
 * Textos y enlaces compartidos entre la generación del mensaje de compartir y
 * la página HTML de vista previa.
 */
public final class EventShareSupport {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private EventShareSupport() {
    }

    /**
     * Construye la URL del evento en el frontend.
     *
     * @param event    evento.
     * @param settings configuración.
     * @return URL absoluta.
     */
    public static String eventUrl(Event event, CalendarSettings settings) {
        String path = settings.frontendEventPath() != null
                ? settings.frontendEventPath().replace("{id}", event.getId().value())
                : "/eventos/" + event.getId().value();
        return joinUrl(settings.frontendBaseUrl(), path);
    }

    /**
     * Construye la URL de descarga del .ics.
     *
     * @param event    evento.
     * @param settings configuración.
     * @return URL absoluta.
     */
    public static String icsUrl(Event event, CalendarSettings settings) {
        return joinUrl(settings.apiBaseUrl(),
                "/api/v1/public/events/" + event.getId().value() + "/calendar.ics");
    }

    /**
     * Describe fecha, horario y lugar del evento.
     *
     * @param event evento.
     * @return texto descriptivo.
     */
    public static String description(Event event) {
        return scheduleText(event) + " · " + event.getLocation();
    }

    /**
     * Genera el texto sugerido para compartir.
     *
     * @param event    evento.
     * @param settings configuración.
     * @return mensaje para redes o mensajería.
     */
    public static String message(Event event, CalendarSettings settings) {
        return event.getTitle() + " — " + description(event) + ". " + eventUrl(event, settings);
    }

    /**
     * Describe la programación en español.
     *
     * @param event evento.
     * @return texto de fecha y horario.
     */
    public static String scheduleText(Event event) {
        if (event.getStartTime() != null) {
            return event.getStartDate().format(DATE)
                    + " de " + event.getStartTime().format(TIME)
                    + " a " + event.getEndTime().format(TIME);
        }
        if (event.getStartDate().equals(event.getEndDate())) {
            return event.getStartDate().format(DATE) + " (todo el día)";
        }
        return "Del " + event.getStartDate().format(DATE)
                + " al " + event.getEndDate().format(DATE);
    }

    /**
     * Une una URL base y una ruta evitando barras duplicadas.
     *
     * @param base URL base.
     * @param path ruta.
     * @return URL resultante.
     */
    public static String joinUrl(String base, String path) {
        String normalizedBase = base == null ? "" : base.strip();
        String normalizedPath = path == null ? "" : path.strip();
        if (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return normalizedBase + normalizedPath;
    }
}
