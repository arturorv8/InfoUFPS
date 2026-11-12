package org.infoufps.application.academic;

import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.academic.TipoMateria;

public record ModerateEntityCommand(
        // Correcciones opcionales para Profesor
        String nuevoNombre,
        Facultad nuevaFacultad,
        String nuevoEmail,
        // Correcciones opcionales para Materia
        String nuevoCodigo,
        Integer nuevosCreditos,
        TipoMateria nuevoTipo
) {
    public static ModerateEntityCommand empty() {
        return new ModerateEntityCommand(null, null, null, null, null, null);
    }
}

