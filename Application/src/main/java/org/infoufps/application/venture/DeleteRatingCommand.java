package org.infoufps.application.venture;

/**
 * Comando para borrar la calificación otorgada a un emprendimiento.
 */
public record DeleteRatingCommand(
        String ventureId,
        String accountId
) {
}

