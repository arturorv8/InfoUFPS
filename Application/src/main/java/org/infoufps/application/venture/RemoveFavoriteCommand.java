package org.infoufps.application.venture;

/**
 * Comando para remover un emprendimiento de favoritos.
 */
public record RemoveFavoriteCommand(
        String ventureId,
        String accountId
) {
}

