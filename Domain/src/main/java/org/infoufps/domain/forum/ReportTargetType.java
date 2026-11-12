package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Tipo de destino de un reporte de moderación dentro del foro.
 */
public enum ReportTargetType {
    THREAD,
    COMMENT;

    public static ReportTargetType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El tipo de destino del reporte es obligatorio");
        }
        try {
            return ReportTargetType.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Tipo de destino inválido: " + value + ". Valores permitidos: THREAD, COMMENT");
        }
    }
}

