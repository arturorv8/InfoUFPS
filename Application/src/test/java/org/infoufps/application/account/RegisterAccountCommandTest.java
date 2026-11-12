package org.infoufps.application.account;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterAccountCommandTest {

    @Test
    @DisplayName("El email es obligatorio")
    void rejectsBlankEmail() {
        assertThrows(DomainException.class, () -> new RegisterAccountCommand(" ", "password123", "Nombre"));
    }

    @Test
    @DisplayName("La contraseña debe tener al menos 8 caracteres")
    void rejectsShortPassword() {
        assertThrows(DomainException.class, () -> new RegisterAccountCommand("user@ufps.edu.co", "corta", "Nombre"));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void rejectsBlankName() {
        assertThrows(DomainException.class, () -> new RegisterAccountCommand("user@ufps.edu.co", "password123", " "));
    }

    @Test
    @DisplayName("Un comando válido se construye correctamente")
    void acceptsValidCommand() {
        assertDoesNotThrow(() -> new RegisterAccountCommand("user@ufps.edu.co", "password123", "Nombre"));
    }
}
