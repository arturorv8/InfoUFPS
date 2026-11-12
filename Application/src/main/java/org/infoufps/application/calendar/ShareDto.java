package org.infoufps.application.calendar;

/**
 * DTO con la información necesaria para compartir un evento.
 *
 * @param message          texto sugerido para compartir.
 * @param eventUrl         enlace al evento en el frontend.
 * @param googleCalendarUrl enlace directo para añadir el evento a Google Calendar.
 * @param icsUrl           enlace de descarga del archivo iCalendar.
 */
public record ShareDto(
        String message,
        String eventUrl,
        String googleCalendarUrl,
        String icsUrl
) {
}
