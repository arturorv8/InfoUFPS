package org.infoufps.infrastructure.venture.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'favorite_ventures'.
 */
@Entity
@Table(name = "favorite_ventures",
        indexes = {
                @Index(name = "idx_favorite_ventures_account", columnList = "account_id")
        })
public class FavoriteVentureJpa {

    @EmbeddedId
    private FavoriteVentureIdJpa id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FavoriteVentureJpa() {}

    public FavoriteVentureJpa(FavoriteVentureIdJpa id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public FavoriteVentureIdJpa getId() { return id; }
    public void setId(FavoriteVentureIdJpa id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

