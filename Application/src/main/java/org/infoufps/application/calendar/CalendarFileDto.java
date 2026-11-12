package org.infoufps.application.calendar;

/**
 * Archivo iCalendar generado para un evento.
 *
 * @param fileName nombre sugerido del archivo.
 * @param content  contenido completo en formato iCalendar.
 */
public record CalendarFileDto(String fileName, String content) {
}
