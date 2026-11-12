package org.infoufps.application.content;

import org.infoufps.application.academic.PublicMateriaListItemDto;
import org.infoufps.application.academic.PublicProfesorListItemDto;

import java.util.List;

public record GlobalSearchResultDto(
        String query,
        List<ModuleItemDto> contenidos,
        List<PublicProfesorListItemDto> profesores,
        List<PublicMateriaListItemDto> materias
) {
}
