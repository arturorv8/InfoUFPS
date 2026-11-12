package org.infoufps.application.calendar;

/**
 * Comando para editar o corregir un evento.
 *
 * @param eventId   identificador del evento.
 * @param accountId cuenta que edita.
 * @param admin     true si el actor es administrador.
 * @param data      nuevos datos del evento.
 */
public record UpdateEventCommand(String eventId, String accountId, boolean admin, EventRequest data) {
}
