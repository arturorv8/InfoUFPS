package org.infoufps.domain.account;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.valueobjects.Email;

import java.time.LocalDateTime;
import java.util.Objects;

public class Account {

    private AccountId accountId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private AccountStatus status;
    private boolean emailVerified = true;
    private LocalDateTime createdAt;

    private Account(AccountId accountId, String name, String email, String passwordHash, Role role) {
        this.accountId = Objects.requireNonNull(accountId, "accountId es obligatorio");
        this.name = Objects.requireNonNull(name, "nombre es obligatorio");
        this.email = Objects.requireNonNull(email, "email es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.role = role != null ? role : Role.USER;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = BogotaTime.now();
    }

    public static Account register(String email, String passwordHash, String name) {
        return register(email, passwordHash, name, Role.USER);
    }

    public static Account register(String email, String passwordHash, String name, Role role) {
        String normalizedEmail = Email.normalize(email);
        if (normalizedEmail == null) {
            throw new DomainException("El email es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la cuenta es obligatorio");
        }
        return new Account(AccountId.generate(), name, normalizedEmail, passwordHash, role);
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, LocalDateTime createdAt) {
        return of(accountId, name, email, passwordHash, Role.USER, createdAt);
    }

    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, Role role, LocalDateTime createdAt) {
        return of(accountId, name, email, passwordHash, role, AccountStatus.ACTIVE, createdAt);
    }

    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, Role role, AccountStatus status,
                             LocalDateTime createdAt) {
        return of(accountId, name, email, passwordHash, role, status, true, createdAt);
    }

    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, Role role, AccountStatus status,
                             boolean emailVerified, LocalDateTime createdAt) {
        Account account = new Account(accountId, name, email, passwordHash, role);
        account.status = status != null ? status : AccountStatus.ACTIVE;
        account.emailVerified = emailVerified;
        account.createdAt = createdAt;
        return account;
    }

    /**
     * @return {@code true} si la cuenta tiene el correo verificado.
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void unverifyEmail() {
        this.emailVerified = false;
    }

    /**
     * @return {@code true} si la cuenta puede autenticarse y operar.
     */
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /**
     * Suspende la cuenta. La operación es idempotente.
     */
    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }

    /**
     * Reactiva la cuenta. La operación es idempotente.
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    /**
     * Cambia el rol de la cuenta.
     */
    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole, "El rol es obligatorio");
    }

    /**
     * Actualiza el nombre visible de la cuenta.
     */
    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new DomainException("El nombre de la cuenta es obligatorio");
        }
        String trimmed = newName.strip();
        if (trimmed.length() > 255) {
            throw new DomainException("El nombre no puede superar los 255 caracteres");
        }
        this.name = trimmed;
    }

    /**
     * Reemplaza el hash de la contraseña. La verificación de la contraseña
     * actual corresponde a la capa de aplicación.
     */
    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
        this.passwordHash = newPasswordHash;
    }

    public AccountId getAccountId() { return accountId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
