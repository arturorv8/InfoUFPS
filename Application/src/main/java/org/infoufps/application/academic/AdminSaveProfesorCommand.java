package org.infoufps.application.academic;

import org.infoufps.domain.academic.Facultad;

import java.util.Set;

public record AdminSaveProfesorCommand(
        String nombre,
        Facultad facultad,
        String email,
        Set<String> materiasIds
) {
}

