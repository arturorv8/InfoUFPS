package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IVentureRatingRepository;
import org.infoufps.domain.venture.VentureId;

import java.util.Optional;

/**
 * Consulta de la calificación otorgada por la cuenta autenticada a un emprendimiento.
 */
@ApplicationScoped
public class GetMyVentureRatingQuery {

    private final IVentureRatingRepository ratingRepository;

    @Inject
    public GetMyVentureRatingQuery(IVentureRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Optional<VentureRatingDto> execute(String accountId, String ventureId) {
        return ratingRepository.findByAccountIdAndVentureId(AccountId.of(accountId), VentureId.of(ventureId))
                .map(VentureRatingDto::from);
    }
}

