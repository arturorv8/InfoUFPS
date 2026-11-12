package org.infoufps.domain.academic;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;

public record ProfesorSearchCriteria(
        String query,
        Facultad facultad,
        EstadoModeracion estado
) {
    public static ProfesorSearchCriteria publicSearch(String query, Facultad facultad) {
        return new ProfesorSearchCriteria(query, facultad, EstadoModeracion.PUBLICADO);
    }
}

