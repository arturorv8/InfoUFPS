package org.infoufps.application.venture;

import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.venture.RatingSummary;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureCategory;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO con el detalle completo de un emprendimiento, incluyendo su canal de contacto y calificaciones.
 */
public record VentureDetailDto(
        String id,
        String name,
        String description,
        VentureCategory category,
        String ownerId,
        ContactDto contact,
        Set<ServiceMode> serviceModes,
        String location,
        boolean active,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        double ratingAverage,
        long ratingCount,
        Integer myRating
) {
    public static VentureDetailDto from(Venture v, RatingSummary ratingSummary, Integer myRating) {
        RatingSummary summary = ratingSummary != null ? ratingSummary : RatingSummary.empty();
        return new VentureDetailDto(
                v.getId().value(),
                v.getName(),
                v.getDescription(),
                v.getCategory(),
                v.getOwnerId().value(),
                ContactDto.from(v.getContact()),
                v.getServiceModes(),
                v.getLocation(),
                v.isActive(),
                v.getStatus(),
                v.getCreatedAt(),
                v.getUpdatedAt(),
                v.getPublishedAt(),
                summary.average(),
                summary.total(),
                myRating
        );
    }
}

