package org.infoufps.application.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.TipoMateria;

/**
 * DTO público de una materia en listados.
 * No expone el estado de moderación ni el identificador de la cuenta que la propuso.
 */
public record PublicMateriaListItemDto(
        String id,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String tipoDisplayName,
        Double promedioCalificacion,
        long totalResenas
) {
    public static PublicMateriaListItemDto of(Materia m, Double promedio, long total) {
        return new PublicMateriaListItemDto(
                m.getId().value(),
                m.getNombre(),
                m.getCodigo(),
                m.getCreditos(),
                m.getTipo(),
                m.getTipo() != null ? m.getTipo().getDisplayName() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total
        );
    }
}
