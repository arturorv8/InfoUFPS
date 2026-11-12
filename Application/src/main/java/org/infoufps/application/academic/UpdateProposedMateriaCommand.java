package org.infoufps.application.academic;

import org.infoufps.domain.academic.TipoMateria;

public record UpdateProposedMateriaCommand(
        String materiaId,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String editorId
) {
}

