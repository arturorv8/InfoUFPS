package org.infoufps.application.academic;

import org.infoufps.domain.academic.Facultad;

public record UpdateProposedProfesorCommand(
        String profesorId,
        String nombre,
        Facultad facultad,
        String email,
        String editorId
) {
}

