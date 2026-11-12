package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Modos de prestación del servicio u oferta del emprendimiento.
 */
public enum ServiceMode {
    PRESENCIAL,
    DOMICILIO,
    VIRTUAL;

    /**
     * Resuelve el modo de servicio a partir de una cadena, insensible a mayúsculas/minúsculas.
     *
     * @param value Nombre textual del modo.
     * @return Instancia correspondiente.
     * @throws DomainException Si el valor no coincide con ningún modo de servicio.
     */
    public static ServiceMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.strip().toUpperCase();
        for (ServiceMode mode : values()) {
            if (mode.name().equals(clean)) {
                return mode;
            }
        }
        throw new DomainException("Modo de servicio inválido: " + value);
    }
}

