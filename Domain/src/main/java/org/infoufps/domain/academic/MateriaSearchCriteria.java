package org.infoufps.domain.academic;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;

public record MateriaSearchCriteria(
        String query,
        Integer creditos,
        TipoMateria tipo,
        EstadoModeracion estado
) {
    public static MateriaSearchCriteria publicSearch(String query, Integer creditos, TipoMateria tipo) {
        return new MateriaSearchCriteria(query, creditos, tipo, EstadoModeracion.PUBLICADO);
    }
}

