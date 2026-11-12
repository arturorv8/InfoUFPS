package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.academic.MateriaSearchCriteria;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetModeracionMateriasQuery {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GetModeracionMateriasQuery(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<MateriaDetailDto> execute(EstadoModeracion estado, PageRequest pageRequest) {
        MateriaSearchCriteria criteria = new MateriaSearchCriteria(null, null, null, estado);
        PageResult<Materia> result = materiaRepository.search(criteria, pageRequest);

        List<MateriaId> ids = result.items().stream().map(Materia::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByMateria(ids);

        List<MateriaDetailDto> dtos = result.items().stream().map(m -> {
            RatingSummary summary = summaries.getOrDefault(m.getId().value(), RatingSummary.empty());
            return MateriaDetailDto.of(m, summary.average(), summary.total());
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
