package org.infoufps.application.account;

/**
 * Token de acceso emitido junto con su vigencia en segundos.
 */
public record IssuedAccessToken(String token, long expiresInSeconds) {

    public IssuedAccessToken {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede estar vacío");
        }
        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("La vigencia del token debe ser positiva");
        }
    }
}
