package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;

import java.util.Collections;
import java.util.Set;

/**
 * Criterios de búsqueda y filtrado para el catálogo público de emprendimientos.
 */
public record VentureSearchCriteria(
        String query,
        Set<VentureCategory> categories,
        Set<ServiceMode> modes,
        String sort,
        PageRequest pageRequest
) {

    public static final String SORT_NAME = "name";
    public static final String SORT_RATING = "rating";

    public VentureSearchCriteria {
        if (sort == null || sort.isBlank()) {
            sort = SORT_NAME;
        } else {
            String clean = sort.strip().toLowerCase();
            if (!clean.equals(SORT_NAME) && !clean.equals(SORT_RATING)) {
                throw new DomainException("Criterio de ordenamiento inválido: " + sort + ". Se espera 'name' o 'rating'");
            }
            sort = clean;
        }
        categories = categories != null ? Collections.unmodifiableSet(categories) : Collections.emptySet();
        modes = modes != null ? Collections.unmodifiableSet(modes) : Collections.emptySet();
        pageRequest = pageRequest != null ? pageRequest : PageRequest.first();
    }

    public static VentureSearchCriteria of(String query,
                                           Set<VentureCategory> categories,
                                           Set<ServiceMode> modes,
                                           String sort,
                                           PageRequest pageRequest) {
        return new VentureSearchCriteria(query, categories, modes, sort, pageRequest);
    }
}

