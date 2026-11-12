package org.infoufps.domain.shared;

import org.infoufps.domain.shared.text.TextNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTest {

    @Test
    @DisplayName("Debe normalizar tildes, mayúsculas y caracteres especiales")
    void testNormalizeAccentsAndCase() {
        String input = "  ¡Álvaro Gómez Núñez!  ";
        String normalized = TextNormalizer.normalize(input);
        assertEquals("alvaro gomez nunez", normalized);
    }

    @Test
    @DisplayName("Debe ordenar palabras alfabéticamente para búsqueda sin orden de palabras")
    void testNormalizeAndSortWords() {
        String name1 = "Juan Carlos Pérez";
        String name2 = "Pérez, Juan Carlos";
        String name3 = "CARLOS   JUAN   PÉREZ";

        String canonical1 = TextNormalizer.normalizeAndSortWords(name1);
        String canonical2 = TextNormalizer.normalizeAndSortWords(name2);
        String canonical3 = TextNormalizer.normalizeAndSortWords(name3);

        assertEquals("carlos juan perez", canonical1);
        assertEquals(canonical1, canonical2);
        assertEquals(canonical1, canonical3);
    }

    @Test
    @DisplayName("Debe coincidir tokens sin importar el orden ni tildes")
    void testMatchesQueryTokensDisordered() {
        String candidate = "Profesor Juan David Pérez González";

        assertTrue(TextNormalizer.matchesQueryTokens(candidate, "perez juan"));
        assertTrue(TextNormalizer.matchesQueryTokens(candidate, "GONZÁLEZ DAVID"));
        assertTrue(TextNormalizer.matchesQueryTokens(candidate, "perez"));
        assertFalse(TextNormalizer.matchesQueryTokens(candidate, "perez maria"));
    }

    @Test
    @DisplayName("Debe escapar caracteres especiales de SQL LIKE (% y _)")
    void testEscapeLikeSpecialCharacters() {
        String query = "100%_aprobado\\test";
        String escaped = TextNormalizer.escapeLikePattern(query);
        assertEquals("100\\%\\_aprobado\\\\test", escaped);
    }

    @Test
    @DisplayName("Debe calcular similitud correctamente entre cadenas")
    void testCalculateSimilarity() {
        String s1 = "Juan Pérez";
        String s2 = "Juan Peres";
        double sim = TextNormalizer.calculateSimilarity(s1, s2);
        assertTrue(sim >= 0.8, "La similitud entre 'Juan Pérez' y 'Juan Peres' debe ser alta: " + sim);

        String s3 = "Pedro Gómez";
        double simDiff = TextNormalizer.calculateSimilarity(s1, s3);
        assertTrue(simDiff < 0.5, "La similitud entre nombres distintos debe ser baja: " + simDiff);
    }
}

