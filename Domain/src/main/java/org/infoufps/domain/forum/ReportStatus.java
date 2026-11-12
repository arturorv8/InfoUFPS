package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Estado del ciclo de moderación de un reporte.
 */
public enum ReportStatus {
    PENDING,
    RESOLVED;

    public static ReportStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El estado del reporte es obligatorio");
        }
        try {
            return ReportStatus.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Estado de reporte inválido: " + value + ". Valores permitidos: PENDING, RESOLVED");
        }
    }
}

