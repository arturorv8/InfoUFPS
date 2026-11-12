package org.infoufps.application.calendar;

/**
 * Comando para que un usuario proponga un evento.
 *
 * @param accountId cuenta que propone.
 * @param data      datos del evento.
 */
public record ProposeEventCommand(String accountId, EventRequest data) {
}
