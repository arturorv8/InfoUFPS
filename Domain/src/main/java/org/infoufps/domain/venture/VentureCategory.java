package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Categorías temáticas de los emprendimientos en La Vitrina.
 */
public enum VentureCategory {
    COMIDA_Y_BEBIDAS,
    MODA_Y_ACCESORIOS,
    TECNOLOGIA,
    SERVICIOS_Y_TUTORIAS,
    ARTE_Y_MANUALIDADES,
    SALUD_Y_BIENESTAR,
    OTROS;

    /**
     * Resuelve la categoría a partir de una cadena, insensible a mayúsculas/minúsculas.
     *
     * @param value Nombre textual de la categoría.
     * @return Instancia correspondiente.
     * @throws DomainException Si el valor no coincide con ninguna categoría.
     */
    public static VentureCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.strip().toUpperCase();
        for (VentureCategory category : values()) {
            if (category.name().equals(clean)) {
                return category;
            }
        }
        throw new DomainException("Categoría de emprendimiento inválida: " + value);
    }
}

