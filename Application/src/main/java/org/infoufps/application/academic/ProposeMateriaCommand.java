package org.infoufps.application.academic;

import org.infoufps.domain.academic.TipoMateria;

public record ProposeMateriaCommand(
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String proponenteId
) {
}

