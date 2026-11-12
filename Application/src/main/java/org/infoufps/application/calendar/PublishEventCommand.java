package org.infoufps.application.calendar;

/**
 * Comando para publicar un evento en revisión.
 *
 * @param eventId identificador del evento.
 */
public record PublishEventCommand(String eventId) {
}
