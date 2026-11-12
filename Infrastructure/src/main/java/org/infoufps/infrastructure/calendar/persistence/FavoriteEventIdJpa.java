package org.infoufps.infrastructure.calendar.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta de un favorito: cuenta + evento.
 */
@Embeddable
public class FavoriteEventIdJpa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    public FavoriteEventIdJpa() {
    }

    public FavoriteEventIdJpa(String accountId, String eventId) {
        this.accountId = accountId;
        this.eventId = eventId;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteEventIdJpa that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, eventId);
    }
}
