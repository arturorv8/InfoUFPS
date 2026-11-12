package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Cambio de contraseña de la cuenta autenticada.
 */
public record ChangePasswordCommand(
        String currentPassword,
        String newPassword
) {
    public ChangePasswordCommand {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new DomainException("La contraseña actual es obligatoria");
        }
        PasswordPolicy.validate(newPassword);
    }
}
