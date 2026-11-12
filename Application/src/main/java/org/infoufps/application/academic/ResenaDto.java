package org.infoufps.application.academic;

import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;

import java.time.LocalDateTime;

public record ResenaDto(
        String id,
        String autorId,
        String autorNombre,
        TipoDestino tipoDestino,
        String profesorId,
        String materiaId,
        int calificacion,
        String comentario,
        String materiaCursada,
        EstadoModeracion estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResenaDto of(Resena r, String autorNombre) {
        return new ResenaDto(
                r.getId().value(),
                r.getAutorId().value(),
                autorNombre,
                r.getTipoDestino(),
                r.getProfesorId() != null ? r.getProfesorId().value() : null,
                r.getMateriaId() != null ? r.getMateriaId().value() : null,
                r.getCalificacion(),
                r.getComentario(),
                r.getMateriaCursada(),
                r.getEstado(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

