package org.infoufps.application.calendar;

/**
 * Comando para archivar un evento.
 *
 * @param eventId identificador del evento.
 */
public record ArchiveEventCommand(String eventId) {
}
