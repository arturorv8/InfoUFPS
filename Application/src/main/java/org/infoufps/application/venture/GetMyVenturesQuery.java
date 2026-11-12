package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.*;

import java.util.List;
import java.util.Map;

/**
 * Consulta de los emprendimientos registrados por la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyVenturesQuery {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public GetMyVenturesQuery(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public List<VentureDetailDto> execute(String ownerId) {
        AccountId accountId = AccountId.of(ownerId);
        List<Venture> ventures = ventureRepository.findByOwnerId(accountId);

        List<VentureId> ids = ventures.stream().map(Venture::getId).toList();
        Map<VentureId, RatingSummary> summaries = ratingRepository.getRatingSummaries(ids);

        return ventures.stream()
                .map(v -> VentureDetailDto.from(v, summaries.get(v.getId()), null))
                .toList();
    }
}

