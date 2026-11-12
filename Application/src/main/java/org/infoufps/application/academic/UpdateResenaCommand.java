package org.infoufps.application.academic;

public record UpdateResenaCommand(
        String resenaId,
        String autorId,
        int calificacion,
        String comentario,
        String materiaCursada
) {
}

