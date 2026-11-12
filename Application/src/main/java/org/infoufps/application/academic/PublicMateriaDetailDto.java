package org.infoufps.application.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.TipoMateria;

import java.time.LocalDateTime;

/**
 * DTO público del detalle de una materia.
 * No expone el estado de moderación ni el identificador de la cuenta que la propuso.
 */
public record PublicMateriaDetailDto(
        String id,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String tipoDisplayName,
        Double promedioCalificacion,
        long totalResenas,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PublicMateriaDetailDto of(Materia m, Double promedio, long total) {
        return new PublicMateriaDetailDto(
                m.getId().value(),
                m.getNombre(),
                m.getCodigo(),
                m.getCreditos(),
                m.getTipo(),
                m.getTipo() != null ? m.getTipo().getDisplayName() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total,
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
