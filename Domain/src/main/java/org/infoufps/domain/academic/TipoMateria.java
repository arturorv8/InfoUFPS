package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.text.Normalizer;

public enum TipoMateria {
    CARRERA("carrera"),
    ELECTIVA("electiva"),
    CURSO_DE_FORMACION("curso de formación");

    private final String displayName;

    TipoMateria(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TipoMateria fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String clean = cleanText(text);
        for (TipoMateria t : values()) {
            if (t.name().equalsIgnoreCase(text.trim()) || cleanText(t.displayName).equalsIgnoreCase(clean)) {
                return t;
            }
        }
        throw new DomainException("Tipo de materia desconocido: " + text);
    }

    private static String cleanText(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

