package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

public enum TipoDestino {
    PROFESOR,
    MATERIA;

    public static TipoDestino fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (TipoDestino t : values()) {
            if (t.name().equalsIgnoreCase(text.trim())) {
                return t;
            }
        }
        throw new DomainException("Tipo de destino desconocido: " + text);
    }
}

