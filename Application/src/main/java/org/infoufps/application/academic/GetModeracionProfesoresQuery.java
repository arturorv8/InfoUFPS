package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetModeracionProfesoresQuery {

    private final IProfesorRepository profesorRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GetModeracionProfesoresQuery(IProfesorRepository profesorRepository, IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<ProfesorDetailDto> execute(EstadoModeracion estado, PageRequest pageRequest) {
        ProfesorSearchCriteria criteria = new ProfesorSearchCriteria(null, null, estado);
        PageResult<Profesor> result = profesorRepository.search(criteria, pageRequest);

        List<ProfesorId> ids = result.items().stream().map(Profesor::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByProfesor(ids);

        List<ProfesorDetailDto> dtos = result.items().stream().map(p -> {
            RatingSummary summary = summaries.getOrDefault(p.getId().value(), RatingSummary.empty());
            return ProfesorDetailDto.of(p, summary.average(), summary.total(), List.of(), true);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
