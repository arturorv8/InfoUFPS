package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.List;
import java.util.Map;

/**
 * Consulta de catálogo público de emprendimientos visibles con paginación, filtros y ordenamiento.
 */
@ApplicationScoped
public class SearchVenturesQuery {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public SearchVenturesQuery(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public PageResult<VentureCardDto> execute(VentureSearchCriteria criteria) {
        PageResult<Venture> venturesPage = ventureRepository.search(criteria);

        List<VentureId> ids = venturesPage.items().stream().map(Venture::getId).toList();
        Map<VentureId, RatingSummary> summaries = ratingRepository.getRatingSummaries(ids);

        List<VentureCardDto> cards = venturesPage.items().stream()
                .map(v -> VentureCardDto.from(v, summaries.get(v.getId())))
                .toList();

        return PageResult.of(cards, venturesPage.total(), criteria.pageRequest());
    }
}

