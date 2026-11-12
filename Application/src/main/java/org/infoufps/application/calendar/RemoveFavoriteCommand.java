package org.infoufps.application.calendar;

/**
 * Comando para quitar un evento de favoritos.
 *
 * @param accountId cuenta.
 * @param eventId   evento.
 */
public record RemoveFavoriteCommand(String accountId, String eventId) {
}
