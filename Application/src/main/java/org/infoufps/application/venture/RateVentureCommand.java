package org.infoufps.application.venture;

/**
 * Comando para registrar o actualizar la calificación de un emprendimiento.
 */
public record RateVentureCommand(
        String ventureId,
        String accountId,
        int score
) {
}

