package org.infoufps.application.venture;

import org.infoufps.domain.venture.VentureRating;

import java.time.LocalDateTime;

/**
 * DTO que representa una calificación de emprendimiento.
 */
public record VentureRatingDto(
        String accountId,
        String ventureId,
        int score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VentureRatingDto from(VentureRating rating) {
        if (rating == null) {
            return null;
        }
        return new VentureRatingDto(
                rating.getAccountId().value(),
                rating.getVentureId().value(),
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}

