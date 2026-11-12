package org.infoufps.infrastructure.calendar.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad de Persistencia (JPA/Hibernate) para la tabla 'favorite_events'.
 * La clave primaria compuesta garantiza la unicidad por (account_id, event_id).
 */
@Entity
@Table(name = "favorite_events", indexes = {
        @Index(name = "idx_favorite_events_event_id", columnList = "event_id")
})
public class FavoriteEventJpa {

    @EmbeddedId
    private FavoriteEventIdJpa id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FavoriteEventJpa() {
    }

    public FavoriteEventIdJpa getId() { return id; }
    public void setId(FavoriteEventIdJpa id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
