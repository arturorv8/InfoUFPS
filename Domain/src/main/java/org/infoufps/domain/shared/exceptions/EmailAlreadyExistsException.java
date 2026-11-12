package org.infoufps.domain.shared.exceptions;

/**
 * Excepción para intentos de registro con un email ya existente.
 * Hereda de {@link ConflictException} para que la capa Api la traduzca a HTTP 409.
 */
public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException() {
        super("Ya existe una cuenta con ese email");
    }
}
