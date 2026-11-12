package org.infoufps.application.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;

import java.time.LocalDateTime;

public record MateriaDetailDto(
        String id,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String tipoDisplayName,
        EstadoModeracion estado,
        String proponenteId,
        Double promedioCalificacion,
        long totalResenas,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MateriaDetailDto of(Materia m, Double promedio, long total) {
        return new MateriaDetailDto(
                m.getId().value(),
                m.getNombre(),
                m.getCodigo(),
                m.getCreditos(),
                m.getTipo(),
                m.getTipo() != null ? m.getTipo().getDisplayName() : null,
                m.getEstado(),
                m.getProponenteId() != null ? m.getProponenteId().value() : null,
                promedio != null ? Math.round(promedio * 10.0) / 10.0 : null,
                total,
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}

