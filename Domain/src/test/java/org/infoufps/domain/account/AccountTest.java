package org.infoufps.domain.account;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Account: normalización y validación del email")
class AccountTest {

    @Test
    @DisplayName("Normaliza el email eliminando espacios y pasando a minúsculas")
    void registerNormalizesEmail() {
        Account account = Account.register("  Usuario@UFPS.EDU.CO ", "hash", "Usuario");

        assertEquals("usuario@ufps.edu.co", account.getEmail());
        assertEquals(Role.USER, account.getRole());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    @DisplayName("Rechaza un email con formato inválido")
    void registerRejectsInvalidEmail() {
        assertThrows(DomainException.class,
                () -> Account.register("correo-invalido", "hash", "Usuario"));
    }

    @Test
    @DisplayName("Rechaza un email vacío")
    void registerRejectsBlankEmail() {
        assertThrows(DomainException.class,
                () -> Account.register("   ", "hash", "Usuario"));
    }

    @Test
    @DisplayName("Rechaza un hash de contraseña vacío")
    void registerRejectsBlankPasswordHash() {
        assertThrows(DomainException.class,
                () -> Account.register("usuario@ufps.edu.co", " ", "Usuario"));
    }

    @Test
    @DisplayName("Rechaza un nombre vacío")
    void registerRejectsBlankName() {
        assertThrows(DomainException.class,
                () -> Account.register("usuario@ufps.edu.co", "hash", " "));
    }

    @Test
    @DisplayName("Reconstruye una cuenta desde persistencia conservando el rol y la fecha")
    void reconstructsFromPersistence() {
        AccountId id = AccountId.generate();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30);

        Account account = Account.of(id, "Administrador", "admin@ufps.edu.co",
                "hash", Role.ADMIN, createdAt);

        assertEquals(id, account.getAccountId());
        assertEquals("Administrador", account.getName());
        assertEquals("admin@ufps.edu.co", account.getEmail());
        assertEquals(Role.ADMIN, account.getRole());
        assertEquals(createdAt, account.getCreatedAt());
    }

    @Test
    @DisplayName("Una cuenta recién registrada está activa")
    void newAccountIsActive() {
        Account account = Account.register("usuario@ufps.edu.co", "hash", "Usuario");

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.isActive());
    }

    @Test
    @DisplayName("Suspender y reactivar es idempotente y cambia isActive")
    void suspendAndActivateAreIdempotent() {
        Account account = Account.register("usuario@ufps.edu.co", "hash", "Usuario");

        account.suspend();
        account.suspend();
        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
        assertFalse(account.isActive());

        account.activate();
        account.activate();
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.isActive());
    }

    @Test
    @DisplayName("Reconstruye una cuenta suspendida conservando el estado")
    void reconstructsSuspendedAccount() {
        Account account = Account.of(AccountId.generate(), "Usuario", "usuario@ufps.edu.co",
                "hash", Role.USER, AccountStatus.SUSPENDED, LocalDateTime.now());

        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
        assertFalse(account.isActive());
    }

    @Test
    @DisplayName("Cambiar el rol actualiza el rol de la cuenta")
    void changeRoleUpdatesRole() {
        Account account = Account.register("usuario@ufps.edu.co", "hash", "Usuario");

        account.changeRole(Role.ADMIN);

        assertEquals(Role.ADMIN, account.getRole());
    }
}
