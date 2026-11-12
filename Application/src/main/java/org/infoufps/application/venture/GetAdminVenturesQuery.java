package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.List;
import java.util.Map;

/**
 * Consulta de la cola de moderación de emprendimientos para el administrador.
 * Incluye marcas de advertencia de duplicados (mismo nombre o mismo contacto con distinto dueño).
 */
@ApplicationScoped
public class GetAdminVenturesQuery {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public GetAdminVenturesQuery(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public PageResult<AdminVentureDto> execute(Status status, PageRequest pageRequest) {
        PageResult<Venture> page = ventureRepository.findAdmin(status, pageRequest);

        List<VentureId> ids = page.items().stream().map(Venture::getId).toList();
        Map<VentureId, RatingSummary> summaries = ratingRepository.getRatingSummaries(ids);

        List<AdminVentureDto> dtos = page.items().stream().map(v -> {
            boolean duplicateName = ventureRepository.existsByNameSearchAndDifferentOwner(v.getNameSearch(), v.getOwnerId());
            boolean duplicateContact = ventureRepository.existsByContactValueAndDifferentOwner(v.getContact().value(), v.getOwnerId());
            return AdminVentureDto.from(v, duplicateName, duplicateContact, summaries.get(v.getId()));
        }).toList();

        return PageResult.of(dtos, page.total(), pageRequest);
    }
}

