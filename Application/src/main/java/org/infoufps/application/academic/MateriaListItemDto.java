package org.infoufps.application.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;

public record MateriaListItemDto(
        String id,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String tipoDisplayName,
        EstadoModeracion estado,
        Double promedioCalificacion,
        long totalResenas
) {
    public static MateriaListItemDto of(Materia m, Double promedio, long total) {
        return new MateriaListItemDto(
                m.getId().value(),
                m.getNombre(),
                m.getCodigo(),
                m.getCreditos(),
                m.getTipo(),
                m.getTipo() != null ? m.getTipo().getDisplayName() : null,
                m.getEstado(),
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total
        );
    }
}

