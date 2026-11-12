package org.infoufps.application.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.TipoMateria;

public record MateriaSimpleDto(
        String id,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String tipoDisplayName
) {
    public static MateriaSimpleDto from(Materia m) {
        return new MateriaSimpleDto(
                m.getId().value(),
                m.getNombre(),
                m.getCodigo(),
                m.getCreditos(),
                m.getTipo(),
                m.getTipo() != null ? m.getTipo().getDisplayName() : null
        );
    }
}

