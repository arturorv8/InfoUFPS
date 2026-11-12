package org.infoufps.application.venture;

/**
 * Comando para marcar un emprendimiento como favorito.
 */
public record AddFavoriteCommand(
        String ventureId,
        String accountId
) {
}

