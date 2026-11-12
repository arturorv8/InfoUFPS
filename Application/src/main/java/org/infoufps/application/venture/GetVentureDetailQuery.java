package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.*;

/**
 * Consulta del detalle completo de un emprendimiento visible para el público.
 */
@ApplicationScoped
public class GetVentureDetailQuery {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public GetVentureDetailQuery(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public VentureDetailDto execute(String id, String currentAccountId) {
        Venture venture = ventureRepository.findById(VentureId.of(id))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + id));

        if (!venture.isVisible()) {
            throw new NotFoundException("Emprendimiento no encontrado o no disponible");
        }

        RatingSummary ratingSummary = ratingRepository.getRatingSummary(venture.getId());

        Integer myRating = null;
        if (currentAccountId != null && !currentAccountId.isBlank()) {
            myRating = ratingRepository.findByAccountIdAndVentureId(AccountId.of(currentAccountId), venture.getId())
                    .map(VentureRating::getScore)
                    .orElse(null);
        }

        return VentureDetailDto.from(venture, ratingSummary, myRating);
    }
}

