package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;

public record RegisterAccountCommand(
        String email,
        String password,
        String name
) {
    public RegisterAccountCommand {
        if (email == null || email.isBlank())
            throw new DomainException("El email es obligatorio");
        PasswordPolicy.validate(password);
        if (name == null || name.isBlank())
            throw new DomainException("El nombre es obligatorio");
    }
}
