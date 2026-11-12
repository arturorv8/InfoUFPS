package org.infoufps.application.venture;

import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.venture.RatingSummary;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureCategory;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para la bandeja de moderación del administrador, con indicadores de duplicado y contacto visible.
 */
public record AdminVentureDto(
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
        boolean duplicateNameWarning,
        boolean duplicateContactWarning,
        double ratingAverage,
        long ratingCount
) {
    public static AdminVentureDto from(Venture v,
                                       boolean duplicateNameWarning,
                                       boolean duplicateContactWarning,
                                       RatingSummary ratingSummary) {
        RatingSummary summary = ratingSummary != null ? ratingSummary : RatingSummary.empty();
        return new AdminVentureDto(
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
                duplicateNameWarning,
                duplicateContactWarning,
                summary.average(),
                summary.total()
        );
    }
}

