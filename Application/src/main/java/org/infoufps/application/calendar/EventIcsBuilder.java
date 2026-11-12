package org.infoufps.application.calendar;

import org.infoufps.application.calendar.CalendarFileDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.time.BogotaTime;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Generador de archivos iCalendar (.ics) para eventos publicados.
 * <p>
 * Reglas aplicadas:
 * <ul>
 *   <li>Eventos con horas: se convierten de America/Bogota a UTC.</li>
 *   <li>Eventos de todo el día o de varios días: DTEND es exclusivo (último día + 1).</li>
 *   <li>UID estable por evento y SEQUENCE igual a la revisión.</li>
 *   <li>Se escapan comas, punto y coma, barras invertidas y saltos de línea.</li>
 *   <li>Las líneas se parten a 75 octetos y se usa CRLF.</li>
 * </ul>
 */
public final class EventIcsBuilder {

    private static final DateTimeFormatter UTC_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter BASIC_DATE =
            DateTimeFormatter.BASIC_ISO_DATE;

    private EventIcsBuilder() {
    }

    /**
     * Genera el archivo iCalendar de un evento.
     *
     * @param event evento publicado.
     * @return archivo con nombre y contenido.
     */
    public static CalendarFileDto build(Event event) {
        StringBuilder content = new StringBuilder();

        appendLine(content, "BEGIN:VCALENDAR");
        appendLine(content, "VERSION:2.0");
        appendLine(content, "PRODID:-//InfoUFPS//Calendario//ES");
        appendLine(content, "CALSCALE:GREGORIAN");
        appendLine(content, "METHOD:PUBLISH");
        appendLine(content, "BEGIN:VEVENT");
        appendLine(content, "UID:event-" + event.getId().value() + "@infoufps");
        appendLine(content, "DTSTAMP:" + BogotaTime.nowUtc().format(UTC_DATE_TIME));
        appendLine(content, "SEQUENCE:" + event.getRevision());
        appendLine(content, "SUMMARY:" + escapeText(event.getTitle()));

        if (event.isAllDay()) {
            appendLine(content, "DTSTART;VALUE=DATE:" + event.getStartDate().format(BASIC_DATE));
            appendLine(content, "DTEND;VALUE=DATE:" + event.getEndDate().plusDays(1).format(BASIC_DATE));
        } else {
            appendLine(content, "DTSTART:" + event.getSchedule().startUtc().format(UTC_DATE_TIME));
            appendLine(content, "DTEND:" + event.getSchedule().endUtc().format(UTC_DATE_TIME));
        }

        appendLine(content, "LOCATION:" + escapeText(event.getLocation()));

        String description = buildDescription(event);
        if (!description.isBlank()) {
            appendLine(content, "DESCRIPTION:" + escapeText(description));
        }
        if (event.getInfoUrl() != null && !event.getInfoUrl().isBlank()) {
            appendLine(content, "URL:" + event.getInfoUrl());
        }

        appendLine(content, "STATUS:CONFIRMED");
        appendLine(content, "END:VEVENT");
        appendLine(content, "END:VCALENDAR");

        return new CalendarFileDto("evento-" + event.getId().value() + ".ics", content.toString());
    }

    private static String buildDescription(Event event) {
        StringBuilder description = new StringBuilder();
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            description.append(event.getDescription().strip());
        }
        if (event.getInfoUrl() != null && !event.getInfoUrl().isBlank()) {
            if (!description.isEmpty()) {
                description.append("\n\n");
            }
            description.append("Más información: ").append(event.getInfoUrl());
        }
        return description.toString();
    }

    private static void appendLine(StringBuilder target, String line) {
        target.append(fold(line)).append("\r\n");
    }

    /**
     * Escapa un valor de texto iCalendar.
     *
     * @param value valor original.
     * @return valor escapado.
     */
    public static String escapeText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }

    /**
     * Parte una línea en fragmentos de máximo 75 octetos UTF-8. Las líneas de
     * continuación comienzan con un espacio.
     *
     * @param line línea original.
     * @return línea plegada con CRLF internos.
     */
    public static String fold(String line) {
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 75) {
            return line;
        }

        StringBuilder folded = new StringBuilder();
        int index = 0;
        int maxBytes = 75;
        while (index < bytes.length) {
            int take = Math.min(maxBytes, bytes.length - index);
            // No cortar un carácter UTF-8 multibyte por la mitad.
            while (take > 1 && index + take < bytes.length && (bytes[index + take] & 0xC0) == 0x80) {
                take--;
            }
            folded.append(new String(bytes, index, take, StandardCharsets.UTF_8));
            index += take;
            maxBytes = 74; // la línea de continuación incluye un espacio inicial
            if (index < bytes.length) {
                folded.append("\r\n ");
            }
        }
        return folded.toString();
    }
}
