package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Canales o tipos de contacto admitidos para un emprendimiento.
 */
public enum ContactType {
    WHATSAPP,
    INSTAGRAM,
    EMAIL,
    WEB;

    /**
     * Resuelve el tipo de contacto a partir de una cadena, insensible a mayúsculas/minúsculas.
     *
     * @param value Nombre textual del tipo.
     * @return Instancia correspondiente.
     * @throws DomainException Si el valor no coincide con ningún tipo de contacto.
     */
    public static ContactType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.strip().toUpperCase();
        for (ContactType type : values()) {
            if (type.name().equals(clean)) {
                return type;
            }
        }
        throw new DomainException("Tipo de contacto inválido: " + value);
    }
}

