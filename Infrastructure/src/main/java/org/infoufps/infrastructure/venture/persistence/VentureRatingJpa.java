package org.infoufps.infrastructure.venture.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'venture_ratings'.
 */
@Entity
@Table(name = "venture_ratings",
        indexes = {
                @Index(name = "idx_venture_ratings_venture", columnList = "venture_id")
        })
public class VentureRatingJpa {

    @EmbeddedId
    private VentureRatingIdJpa id;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public VentureRatingJpa() {}

    public VentureRatingJpa(VentureRatingIdJpa id, int score, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.score = score;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public VentureRatingIdJpa getId() { return id; }
    public void setId(VentureRatingIdJpa id) { this.id = id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

