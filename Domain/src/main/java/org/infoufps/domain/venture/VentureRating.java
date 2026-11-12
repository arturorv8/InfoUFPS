package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa la calificación otorgada por una cuenta a un emprendimiento.
 * Solo contiene estrellas (1 a 5), sin texto de reseña.
 */
public class VentureRating {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    private final AccountId accountId;
    private final VentureId ventureId;
    private int score;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private VentureRating(AccountId accountId,
                          VentureId ventureId,
                          int score,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.accountId = Objects.requireNonNull(accountId, "La cuenta es obligatoria");
        this.ventureId = Objects.requireNonNull(ventureId, "El emprendimiento es obligatorio");
        this.score = validateScore(score);
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = Objects.requireNonNull(updatedAt, "La fecha de edición es obligatoria");
    }

    /**
     * Fábrica para crear una nueva calificación.
     */
    public static VentureRating create(AccountId accountId, VentureId ventureId, int score) {
        LocalDateTime now = BogotaTime.now();
        return new VentureRating(accountId, ventureId, score, now, now);
    }

    /**
     * Fábrica para reconstruir desde la persistencia.
     */
    public static VentureRating of(AccountId accountId,
                                   VentureId ventureId,
                                   int score,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new VentureRating(accountId, ventureId, score, createdAt, updatedAt);
    }

    /**
     * Actualiza el puntaje de estrellas.
     */
    public void updateScore(int newScore) {
        this.score = validateScore(newScore);
        this.updatedAt = BogotaTime.now();
    }

    private static int validateScore(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new DomainException("La calificación debe estar entre " + MIN_SCORE + " y " + MAX_SCORE + " estrellas");
        }
        return score;
    }

    public AccountId getAccountId() { return accountId; }
    public VentureId getVentureId() { return ventureId; }
    public int getScore() { return score; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VentureRating that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(ventureId, that.ventureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, ventureId);
    }
}

