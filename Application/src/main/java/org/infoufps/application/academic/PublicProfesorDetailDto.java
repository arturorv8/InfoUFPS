package org.infoufps.application.academic;

import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.Facultad;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO público del detalle de un profesor.
 * No expone email, estado de moderación ni el identificador de la cuenta que
 * propuso el registro.
 */
public record PublicProfesorDetailDto(
        String id,
        String nombre,
        Facultad facultad,
        String facultadDisplayName,
        Double promedioCalificacion,
        long totalResenas,
        List<MateriaSimpleDto> materias,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PublicProfesorDetailDto of(Profesor p,
                                             Double promedio,
                                             long total,
                                             List<MateriaSimpleDto> materias) {
        return new PublicProfesorDetailDto(
                p.getId().value(),
                p.getNombre(),
                p.getFacultad(),
                p.getFacultad() != null ? p.getFacultad().getDisplayName() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total,
                materias != null ? materias : List.of(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
