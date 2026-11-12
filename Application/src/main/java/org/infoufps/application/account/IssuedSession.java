package org.infoufps.application.account;

/**
 * Par de tokens emitidos al iniciar o renovar una sesión.
 */
public record IssuedSession(
        IssuedAccessToken accessToken,
        String refreshToken,
        long refreshExpiresInSeconds
) {
    public IssuedSession {
        if (accessToken == null) {
            throw new IllegalArgumentException("El token de acceso es obligatorio");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("El token de renovación es obligatorio");
        }
        if (refreshExpiresInSeconds <= 0) {
            throw new IllegalArgumentException("La vigencia del token de renovación debe ser positiva");
        }
    }
}
