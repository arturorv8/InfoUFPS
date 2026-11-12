package org.infoufps.application.venture;

import org.infoufps.domain.venture.RatingSummary;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureCategory;

import java.util.Set;

/**
 * DTO para las tarjetas de emprendimiento en los listados públicos y de favoritos.
 * Excluye intencionalmente los datos de contacto.
 */
public record VentureCardDto(
        String id,
        String name,
        String description,
        VentureCategory category,
        Set<ServiceMode> serviceModes,
        String location,
        double ratingAverage,
        long ratingCount
) {
    public static VentureCardDto from(Venture v, RatingSummary ratingSummary) {
        RatingSummary summary = ratingSummary != null ? ratingSummary : RatingSummary.empty();
        return new VentureCardDto(
                v.getId().value(),
                v.getName(),
                v.getDescription(),
                v.getCategory(),
                v.getServiceModes(),
                v.getLocation(),
                summary.average(),
                summary.total()
        );
    }
}

