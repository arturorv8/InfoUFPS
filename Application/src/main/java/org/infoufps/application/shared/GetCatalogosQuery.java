package org.infoufps.application.shared;

import jakarta.enterprise.context.ApplicationScoped;
import org.infoufps.application.shared.CatalogoItemDto;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.academic.TipoMateria;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class GetCatalogosQuery {

    public List<CatalogoItemDto> getFacultades() {
        return Arrays.stream(Facultad.values())
                .filter(f -> f != Facultad.NO_ESPECIFICADA)
                .map(f -> CatalogoItemDto.of(f.name(), f.getDisplayName(), "Facultad de " + f.getDisplayName()))
                .toList();
    }

    public List<CatalogoItemDto> getTiposMateria() {
        return Arrays.stream(TipoMateria.values())
                .map(t -> CatalogoItemDto.of(t.name(), t.getDisplayName(), "Tipo de materia: " + t.getDisplayName()))
                .toList();
    }

    public List<Integer> getCreditos() {
        return List.of(1, 2, 3, 4, 5, 6, 8, 10);
    }
}

