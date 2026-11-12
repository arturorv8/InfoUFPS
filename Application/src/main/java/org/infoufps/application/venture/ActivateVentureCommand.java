package org.infoufps.application.venture;

/**
 * Comando para activar un emprendimiento.
 */
public record ActivateVentureCommand(
        String id,
        String requesterId,
        boolean isAdmin
) {
}

