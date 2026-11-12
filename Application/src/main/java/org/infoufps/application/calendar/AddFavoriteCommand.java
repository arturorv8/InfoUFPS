package org.infoufps.application.calendar;

/**
 * Comando para marcar un evento como favorito.
 *
 * @param accountId cuenta que marca.
 * @param eventId   evento marcado.
 */
public record AddFavoriteCommand(String accountId, String eventId) {
}
