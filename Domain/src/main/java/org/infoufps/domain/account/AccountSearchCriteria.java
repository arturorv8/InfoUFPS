package org.infoufps.domain.account;

/**
 * Filtros de búsqueda de cuentas para administración.
 *
 * @param query  texto libre que se compara contra nombre y email.
 * @param role   rol exacto, o {@code null} para no filtrar.
 * @param status estado exacto, o {@code null} para no filtrar.
 */
public record AccountSearchCriteria(
        String query,
        Role role,
        AccountStatus status
) {
    public AccountSearchCriteria {
        query = (query == null || query.isBlank()) ? null : query.strip();
    }
}
