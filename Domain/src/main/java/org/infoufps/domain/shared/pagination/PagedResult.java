package org.infoufps.domain.shared.pagination;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado inmutable para consultas de Dominio.
 *
 * @param <T> Tipo de elemento contenido en la página.
 */
public record PagedResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResult<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResult<>(
                items != null ? List.copyOf(items) : Collections.emptyList(),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public static <T> PagedResult<T> of(List<T> items, long total, PageRequest request) {
        PageRequest normalized = request != null ? request : PageRequest.first();
        return of(items, normalized.page(), normalized.size(), total);
    }

    public static <T> PagedResult<T> empty(int page, int size) {
        return new PagedResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    public long total() {
        return totalElements;
    }

    public boolean hasNext() {
        return page < totalPages;
    }

    public <R> PagedResult<R> map(Function<? super T, R> mapper) {
        List<R> mappedItems = this.items.stream().map(mapper).toList();
        return new PagedResult<>(mappedItems, this.page, this.size, this.totalElements, this.totalPages);
    }
}

