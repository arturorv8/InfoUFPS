package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.application.academic.PublicProfesorListItemDto;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetProfesoresPublicQuery {

    private final IProfesorRepository profesorRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GetProfesoresPublicQuery(IProfesorRepository profesorRepository, IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<PublicProfesorListItemDto> execute(String query, Facultad facultad, PageRequest pageRequest) {
        ProfesorSearchCriteria criteria = ProfesorSearchCriteria.publicSearch(query, facultad);
        PageResult<Profesor> result = profesorRepository.search(criteria, pageRequest);

        List<ProfesorId> ids = result.items().stream().map(Profesor::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByProfesor(ids);

        List<PublicProfesorListItemDto> dtos = result.items().stream().map(p -> {
            RatingSummary summary = summaries.getOrDefault(p.getId().value(), RatingSummary.empty());
            return PublicProfesorListItemDto.of(p, summary.average(), summary.total());
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
