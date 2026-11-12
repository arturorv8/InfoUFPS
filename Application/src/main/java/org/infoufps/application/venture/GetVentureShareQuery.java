package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureId;

/**
 * Consulta de metadatos para compartir un emprendimiento visible.
 */
@ApplicationScoped
public class GetVentureShareQuery {

    private final IVentureRepository ventureRepository;
    private final VentureSettings settings;

    @Inject
    public GetVentureShareQuery(IVentureRepository ventureRepository, VentureSettings settings) {
        this.ventureRepository = ventureRepository;
        this.settings = settings;
    }

    public VentureShareDto execute(String id) {
        Venture venture = ventureRepository.findById(VentureId.of(id))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + id));

        if (!venture.isVisible()) {
            throw new NotFoundException("Emprendimiento no encontrado o no disponible");
        }

        String shareUrl = settings.frontendBaseUrl() + settings.frontendVenturePath().replace("{id}", venture.getId().value());
        return new VentureShareDto(
                venture.getId().value(),
                venture.getName(),
                venture.getDescription(),
                shareUrl,
                settings.shareLogoUrl()
        );
    }
}

