package org.infoufps.domain.account;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Token opaco de renovación de sesión.
 * <p>
 * El valor que recibe el cliente nunca se almacena: solo se persiste su hash
 * SHA-256. Un token está activo mientras no haya sido revocado ni haya vencido.
 * La rotación revoca el token anterior y emite uno nuevo; reutilizar un token
 * ya revocado es una señal de robo y provoca la revocación de toda la cuenta.
 */
public class RefreshToken {

    private final String tokenId;
    private final AccountId accountId;
    private final String tokenHash;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    private RefreshToken(String tokenId, AccountId accountId, String tokenHash,
                         LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime revokedAt) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new DomainException("El identificador del token es obligatorio");
        }
        this.accountId = Objects.requireNonNull(accountId, "accountId es obligatorio");
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new DomainException("El hash del token es obligatorio");
        }
        if (createdAt == null || expiresAt == null) {
            throw new DomainException("Las fechas del token son obligatorias");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new DomainException("La fecha de vencimiento debe ser posterior a la creación");
        }
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    /**
     * Emite un token nuevo con una vigencia en segundos.
     */
    public static RefreshToken issue(AccountId accountId, String tokenHash, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new DomainException("La vigencia del token debe ser positiva");
        }
        LocalDateTime now = BogotaTime.now();
        return new RefreshToken(
                UUID.randomUUID().toString(),
                accountId,
                tokenHash,
                now,
                now.plusSeconds(ttlSeconds),
                null
        );
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static RefreshToken of(String tokenId, AccountId accountId, String tokenHash,
                                  LocalDateTime createdAt, LocalDateTime expiresAt,
                                  LocalDateTime revokedAt) {
        return new RefreshToken(tokenId, accountId, tokenHash, createdAt, expiresAt, revokedAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return now != null && !now.isBefore(expiresAt);
    }

    public boolean isActive(LocalDateTime now) {
        return !isRevoked() && !isExpired(now);
    }

    /**
     * Revoca el token. La operación es idempotente y conserva la primera fecha
     * de revocación.
     */
    public void revoke(LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now != null ? now : BogotaTime.now();
        }
    }

    public String getTokenId() { return tokenId; }
    public AccountId getAccountId() { return accountId; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
}
