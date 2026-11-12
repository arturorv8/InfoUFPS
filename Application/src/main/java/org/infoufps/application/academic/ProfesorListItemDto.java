package org.infoufps.application.academic;

import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;

public record ProfesorListItemDto(
        String id,
        String nombre,
        Facultad facultad,
        String facultadDisplayName,
        String email,
        EstadoModeracion estado,
        Double promedioCalificacion,
        long totalResenas
) {
    public static ProfesorListItemDto of(Profesor p, Double promedio, long total, boolean showEmail) {
        return new ProfesorListItemDto(
                p.getId().value(),
                p.getNombre(),
                p.getFacultad(),
                p.getFacultad() != null ? p.getFacultad().getDisplayName() : null,
                showEmail ? p.getEmail() : null,
                p.getEstado(),
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total
        );
    }
}

