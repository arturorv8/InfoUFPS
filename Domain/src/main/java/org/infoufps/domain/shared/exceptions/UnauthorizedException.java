package org.infoufps.domain.shared.exceptions;

/**
 * Excepción de dominio para operaciones que requieren una cuenta autenticada.
 * La capa Api la traduce a HTTP 401.
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
