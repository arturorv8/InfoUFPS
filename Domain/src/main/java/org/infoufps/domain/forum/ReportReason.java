package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Motivos predefinidos para reportar un hilo o comentario en el foro.
 */
public enum ReportReason {
    CONTENIDO_INAPROPIADO,
    SPAM,
    INFORMACION_FALSA,
    ACOSO_O_INSULTOS,
    OTRO;

    public static ReportReason fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El motivo del reporte es obligatorio");
        }
        try {
            return ReportReason.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Motivo de reporte inválido: " + value + ". Valores permitidos: CONTENIDO_INAPROPIADO, SPAM, INFORMACION_FALSA, ACOSO_O_INSULTOS, OTRO");
        }
    }
}

