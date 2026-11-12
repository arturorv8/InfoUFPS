package org.infoufps.domain.shared.exceptions;

/**
 * Excepción para recursos duplicados. Hereda de {@link ConflictException} para
 * que la capa Api la traduzca a HTTP 409.
 */
public class DuplicateResourceException extends ConflictException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
