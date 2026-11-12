package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Token de renovación recién generado. El valor en claro solo viaja al cliente;
 * en la base de datos se guarda {@code tokenHash}.
 */
public record GeneratedRefreshToken(
        String rawToken,
        String tokenHash,
        long expiresInSeconds
) {
    public GeneratedRefreshToken {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("El token de renovación es obligatorio");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new DomainException("El hash del token de renovación es obligatorio");
        }
        if (expiresInSeconds <= 0) {
            throw new DomainException("La vigencia del token de renovación debe ser positiva");
        }
    }
}
