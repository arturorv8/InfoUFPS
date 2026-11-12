package org.infoufps.application.academic;

import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.TipoDestino;

import java.time.LocalDateTime;

/**
 * DTO público de una reseña.
 * <p>
 * El nombre visible del autor es información pública para dar contexto a la
 * reseña; el identificador de la cuenta ({@code autorId}) y el estado de
 * moderación no se exponen.
 */
public record PublicResenaDto(
        String id,
        String autorNombre,
        TipoDestino tipoDestino,
        String profesorId,
        String materiaId,
        int calificacion,
        String comentario,
        String materiaCursada,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PublicResenaDto of(Resena r, String autorNombre) {
        return new PublicResenaDto(
                r.getId().value(),
                autorNombre,
                r.getTipoDestino(),
                r.getProfesorId() != null ? r.getProfesorId().value() : null,
                r.getMateriaId() != null ? r.getMateriaId().value() : null,
                r.getCalificacion(),
                r.getComentario(),
                r.getMateriaCursada(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
