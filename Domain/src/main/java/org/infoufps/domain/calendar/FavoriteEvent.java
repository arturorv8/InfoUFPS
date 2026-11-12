package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa un evento marcado como favorito por una cuenta.
 * Es única por la pareja {@code (accountId, eventId)} y se elimina en cascada
 * cuando el evento asociado se elimina.
 */
public class FavoriteEvent {

    private final AccountId accountId;
    private final EventId eventId;
    private final LocalDateTime createdAt;

    private FavoriteEvent(AccountId accountId, EventId eventId, LocalDateTime createdAt) {
        this.accountId = Objects.requireNonNull(accountId, "La cuenta es obligatoria");
        this.eventId = Objects.requireNonNull(eventId, "El evento es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    /**
     * Crea un nuevo favorito con la fecha actual en America/Bogota.
     *
     * @param accountId cuenta que marca el favorito.
     * @param eventId   evento marcado.
     * @return nuevo favorito.
     */
    public static FavoriteEvent create(AccountId accountId, EventId eventId) {
        return new FavoriteEvent(accountId, eventId, BogotaTime.now());
    }

    /**
     * Reconstruye un favorito desde persistencia.
     *
     * @param accountId cuenta que marcó el favorito.
     * @param eventId   evento marcado.
     * @param createdAt fecha de creación.
     * @return favorito reconstruido.
     */
    public static FavoriteEvent of(AccountId accountId, EventId eventId, LocalDateTime createdAt) {
        return new FavoriteEvent(accountId, eventId, createdAt);
    }

    public AccountId getAccountId() { return accountId; }
    public EventId getEventId() { return eventId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteEvent that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, eventId);
    }
}
