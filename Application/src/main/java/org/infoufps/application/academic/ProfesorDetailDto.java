package org.infoufps.application.academic;

import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;

import java.time.LocalDateTime;
import java.util.List;

public record ProfesorDetailDto(
        String id,
        String nombre,
        Facultad facultad,
        String facultadDisplayName,
        String email,
        EstadoModeracion estado,
        String proponenteId,
        Double promedioCalificacion,
        long totalResenas,
        List<MateriaSimpleDto> materias,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProfesorDetailDto of(Profesor p,
                                       Double promedio,
                                       long total,
                                       List<MateriaSimpleDto> materias,
                                       boolean showEmail) {
        return new ProfesorDetailDto(
                p.getId().value(),
                p.getNombre(),
                p.getFacultad(),
                p.getFacultad() != null ? p.getFacultad().getDisplayName() : null,
                showEmail ? p.getEmail() : null,
                p.getEstado(),
                p.getProponenteId() != null ? p.getProponenteId().value() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total,
                materias != null ? materias : List.of(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

