package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.academic.MateriaSearchCriteria;
import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.application.content.GlobalSearchResultDto;
import org.infoufps.application.content.ModuleItemDto;
import org.infoufps.application.academic.PublicMateriaListItemDto;
import org.infoufps.application.academic.PublicProfesorListItemDto;
import org.infoufps.domain.content.Content;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GlobalSearchQuery {

    private final IContentRepository contentRepository;
    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GlobalSearchQuery(IContentRepository contentRepository,
                             IProfesorRepository profesorRepository,
                             IMateriaRepository materiaRepository,
                             IResenaRepository resenaRepository) {
        this.contentRepository = contentRepository;
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public GlobalSearchResultDto execute(String query) {
        String cleanQuery = (query != null) ? query.trim() : "";

        // 1. Contenidos publicados
        List<ModuleItemDto> contenidos = List.of();
        if (contentRepository != null) {
            contenidos = contentRepository.searchPublished(cleanQuery, 10).stream()
                    .map(ModuleItemDto::from)
                    .toList();
        }

        // 2. Profesores publicados
        List<PublicProfesorListItemDto> profesores = List.of();
        if (profesorRepository != null) {
            PageResult<Profesor> profResult = profesorRepository.search(
                    ProfesorSearchCriteria.publicSearch(cleanQuery, null),
                    PageRequest.of(1, 10)
            );
            List<ProfesorId> ids = profResult.items().stream().map(Profesor::getId).toList();
            Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByProfesor(ids);
            profesores = profResult.items().stream().map(p -> {
                RatingSummary summary = summaries.getOrDefault(p.getId().value(), RatingSummary.empty());
                return PublicProfesorListItemDto.of(p, summary.average(), summary.total());
            }).toList();
        }

        // 3. Materias publicadas
        List<PublicMateriaListItemDto> materias = List.of();
        if (materiaRepository != null) {
            PageResult<Materia> matResult = materiaRepository.search(
                    MateriaSearchCriteria.publicSearch(cleanQuery, null, null),
                    PageRequest.of(1, 10)
            );
            List<MateriaId> ids = matResult.items().stream().map(Materia::getId).toList();
            Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByMateria(ids);
            materias = matResult.items().stream().map(m -> {
                RatingSummary summary = summaries.getOrDefault(m.getId().value(), RatingSummary.empty());
                return PublicMateriaListItemDto.of(m, summary.average(), summary.total());
            }).toList();
        }

        return new GlobalSearchResultDto(cleanQuery, contenidos, profesores, materias);
    }
}
