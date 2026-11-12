package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;

public record LoginCommand(String email, String password) {

    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new DomainException("El email es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
    }
}
