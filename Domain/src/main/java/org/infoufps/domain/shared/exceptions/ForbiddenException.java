package org.infoufps.domain.shared.exceptions;

/**
 * Excepción de dominio para operaciones que requieren un rol superior.
 * La capa Api la traduce a HTTP 403.
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(message);
    }
}
