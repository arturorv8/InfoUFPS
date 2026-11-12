package org.infoufps.application.academic;

import org.infoufps.domain.academic.TipoDestino;

public record CreateResenaCommand(
        String autorId,
        TipoDestino tipoDestino,
        String profesorId,
        String materiaId,
        int calificacion,
        String comentario,
        String materiaCursada
) {
}

