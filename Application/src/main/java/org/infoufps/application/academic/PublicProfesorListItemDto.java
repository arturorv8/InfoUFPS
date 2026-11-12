package org.infoufps.application.academic;

import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.Facultad;

/**
 * DTO público de un profesor en listados.
 * No expone datos internos como email, estado de moderación ni el identificador
 * de la cuenta que propuso el registro.
 */
public record PublicProfesorListItemDto(
        String id,
        String nombre,
        Facultad facultad,
        String facultadDisplayName,
        Double promedioCalificacion,
        long totalResenas
) {
    public static PublicProfesorListItemDto of(Profesor p, Double promedio, long total) {
        return new PublicProfesorListItemDto(
                p.getId().value(),
                p.getNombre(),
                p.getFacultad(),
                p.getFacultad() != null ? p.getFacultad().getDisplayName() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total
        );
    }
}
