package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.text.Normalizer;

public enum Facultad {
    CIENCIAS_AGRARIAS_Y_DEL_AMBIENTE("Ciencias Agrarias y del Ambiente"),
    CIENCIAS_BASICAS("Ciencias Básicas"),
    CIENCIAS_EMPRESARIALES("Ciencias Empresariales"),
    CIENCIAS_DE_LA_SALUD("Ciencias de la Salud"),
    EDUCACION_ARTES_Y_HUMANIDADES("Educación, Artes y Humanidades"),
    INGENIERIA("Ingeniería"),
    NO_ESPECIFICADA("No especificada");

    private final String displayName;

    Facultad(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Facultad fromString(String text) {
        if (text == null || text.isBlank()) {
            return NO_ESPECIFICADA;
        }
        String clean = cleanText(text);
        for (Facultad f : values()) {
            if (f.name().equalsIgnoreCase(text.trim()) || cleanText(f.displayName).equalsIgnoreCase(clean)) {
                return f;
            }
        }
        throw new DomainException("Facultad desconocida: " + text);
    }

    private static String cleanText(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

