package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Construye el enlace directo para añadir un evento a Google Calendar usando
 * los mismos datos del archivo .ics.
 */
public final class GoogleCalendarLinkBuilder {

    private static final DateTimeFormatter UTC_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter BASIC_DATE =
            DateTimeFormatter.BASIC_ISO_DATE;

    private GoogleCalendarLinkBuilder() {
    }

    /**
     * Genera el enlace de Google Calendar.
     *
     * @param event evento publicado.
     * @return URL absoluta de Google Calendar.
     */
    public static String build(Event event) {
        String dates;
        if (event.isAllDay()) {
            dates = event.getStartDate().format(BASIC_DATE)
                    + "/" + event.getEndDate().plusDays(1).format(BASIC_DATE);
        } else {
            dates = event.getSchedule().startUtc().format(UTC_DATE_TIME)
                    + "/" + event.getSchedule().endUtc().format(UTC_DATE_TIME);
        }

        StringBuilder details = new StringBuilder();
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            details.append(event.getDescription().strip());
        }
        if (event.getInfoUrl() != null && !event.getInfoUrl().isBlank()) {
            if (!details.isEmpty()) {
                details.append("\n\n");
            }
            details.append("Más información: ").append(event.getInfoUrl());
        }

        return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + encode(event.getTitle())
                + "&dates=" + dates
                + "&details=" + encode(details.toString())
                + "&location=" + encode(event.getLocation());
    }

    private static String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
