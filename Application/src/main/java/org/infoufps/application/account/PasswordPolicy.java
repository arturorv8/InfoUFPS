package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.nio.charset.StandardCharsets;

/**
 * Reglas mínimas de contraseña compartidas por registro y cambio de contraseña.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    /** BCrypt solo considera los primeros 72 bytes de la contraseña. */
    public static final int MAX_BCRYPT_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new DomainException("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES) {
            throw new DomainException("La contraseña no puede superar los " + MAX_BCRYPT_BYTES + " bytes");
        }
    }
}
