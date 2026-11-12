package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Acción elegida por un administrador al resolver un reporte de moderación.
 */
public enum ReportResolutionAction {
    RESTORE,
    ARCHIVE;

    public static ReportResolutionAction fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("La acción de resolución es obligatoria");
        }
        try {
            return ReportResolutionAction.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Acción inválida: " + value + ". Valores permitidos: RESTORE, ARCHIVE");
        }
    }
}

