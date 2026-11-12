package org.infoufps.domain.shared.exceptions;

/**
 * Excepción de dominio para conflictos de negocio, por ejemplo duplicados o
 * límites alcanzados. La capa Api la traduce a HTTP 409.
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
