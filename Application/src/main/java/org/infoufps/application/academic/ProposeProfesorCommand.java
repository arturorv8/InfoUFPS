package org.infoufps.application.academic;

import org.infoufps.domain.academic.Facultad;

import java.util.Set;

public record ProposeProfesorCommand(
        String nombre,
        Facultad facultad,
        String email,
        String proponenteId
) {
}

