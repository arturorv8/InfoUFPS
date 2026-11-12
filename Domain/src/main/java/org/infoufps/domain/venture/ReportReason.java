package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Razones por las que un usuario puede reportar un emprendimiento.
 */
public enum ReportReason {
    CONTENIDO_INAPROPIADO,
    FRAUDE_O_ESTAFA,
    INFORMACION_FALSA,
    CONTACTO_INVALIDO,
    OTRO;

    /**
     * Resuelve el motivo de reporte a partir de una cadena, insensible a mayúsculas/minúsculas.
     *
     * @param value Nombre textual del motivo.
     * @return Instancia correspondiente.
     * @throws DomainException Si el valor no coincide con ningún motivo válido.
     */
    public static ReportReason fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.strip().toUpperCase();
        for (ReportReason reason : values()) {
            if (reason.name().equals(clean)) {
                return reason;
            }
        }
        throw new DomainException("Motivo de reporte inválido: " + value);
    }
}

