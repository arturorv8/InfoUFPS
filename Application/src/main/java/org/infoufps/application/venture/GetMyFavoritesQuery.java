package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.List;
import java.util.Map;

/**
 * Consulta paginada de emprendimientos favoritos de la cuenta autenticada.
 * Solo lista los que están en estado PUBLISHED.
 */
@ApplicationScoped
public class GetMyFavoritesQuery {

    private final IFavoriteVentureRepository favoriteRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public GetMyFavoritesQuery(IFavoriteVentureRepository favoriteRepository,
                               IVentureRatingRepository ratingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
    }

    public PageResult<VentureCardDto> execute(String accountId, PageRequest pageRequest) {
        PageResult<Venture> venturesPage = favoriteRepository.findFavoriteVenturesByAccountId(
                AccountId.of(accountId), pageRequest
        );

        List<VentureId> ids = venturesPage.items().stream().map(Venture::getId).toList();
        Map<VentureId, RatingSummary> summaries = ratingRepository.getRatingSummaries(ids);

        List<VentureCardDto> cards = venturesPage.items().stream()
                .map(v -> VentureCardDto.from(v, summaries.get(v.getId())))
                .toList();

        return PageResult.of(cards, venturesPage.total(), pageRequest);
    }
}

