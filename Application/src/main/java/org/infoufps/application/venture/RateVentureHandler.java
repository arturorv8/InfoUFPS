package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.*;

import java.util.Optional;

/**
 * Caso de uso: Calificar un emprendimiento con un puntaje de 1 a 5 estrellas.
 */
@ApplicationScoped
public class RateVentureHandler {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public RateVentureHandler(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public VentureRatingDto execute(RateVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.ventureId()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.ventureId()));

        if (!venture.isVisible()) {
            throw new DomainException("Solo se pueden calificar emprendimientos visibles y publicados");
        }

        AccountId accountId = AccountId.of(command.accountId());
        if (venture.getOwnerId().equals(accountId)) {
            throw new DomainException("No puedes calificar tu propio emprendimiento");
        }

        Optional<VentureRating> existing = ratingRepository.findByAccountIdAndVentureId(accountId, venture.getId());
        VentureRating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            rating.updateScore(command.score());
        } else {
            rating = VentureRating.create(accountId, venture.getId(), command.score());
        }

        ratingRepository.save(rating);
        return VentureRatingDto.from(rating);
    }
}

