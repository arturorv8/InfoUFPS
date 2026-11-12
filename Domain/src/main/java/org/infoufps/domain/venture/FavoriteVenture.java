package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que modela la marcación de un emprendimiento como favorito por una cuenta.
 */
public class FavoriteVenture {

    private final AccountId accountId;
    private final VentureId ventureId;
    private final LocalDateTime createdAt;

    private FavoriteVenture(AccountId accountId, VentureId ventureId, LocalDateTime createdAt) {
        this.accountId = Objects.requireNonNull(accountId, "La cuenta es obligatoria");
        this.ventureId = Objects.requireNonNull(ventureId, "El emprendimiento es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de registro es obligatoria");
    }

    public static FavoriteVenture create(AccountId accountId, VentureId ventureId) {
        return new FavoriteVenture(accountId, ventureId, BogotaTime.now());
    }

    public static FavoriteVenture of(AccountId accountId, VentureId ventureId, LocalDateTime createdAt) {
        return new FavoriteVenture(accountId, ventureId, createdAt);
    }

    public AccountId getAccountId() { return accountId; }
    public VentureId getVentureId() { return ventureId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteVenture that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(ventureId, that.ventureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, ventureId);
    }
}

