package org.infoufps.application.venture;

/**
 * Comando para desactivar un emprendimiento.
 */
public record DeactivateVentureCommand(
        String id,
        String requesterId,
        boolean isAdmin
) {
}

