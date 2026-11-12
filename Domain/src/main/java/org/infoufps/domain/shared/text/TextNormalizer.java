package org.infoufps.domain.shared.text;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TextNormalizer {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");

    private TextNormalizer() {
    }

    /**
     * Normaliza el texto eliminando tildes/diacríticos, convirtiendo a minúsculas,
     * eliminando caracteres especiales de puntuación y compactando espacios.
     */
    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String decomposed = Normalizer.normalize(text.strip(), Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(decomposed).replaceAll("");
        String lower = withoutDiacritics.toLowerCase(Locale.ROOT);
        String clean = NON_ALPHANUMERIC_PATTERN.matcher(lower).replaceAll(" ");
        return MULTIPLE_SPACES_PATTERN.matcher(clean).replaceAll(" ").trim();
    }

    /**
     * Normaliza el texto y ordena sus palabras alfabéticamente.
     * Permite comparar y canonicalizar nombres independientemente del orden de las palabras
     * (por ejemplo: "Juan Carlos Pérez" y "Pérez Juan Carlos" resultan en "carlos juan perez").
     */
    public static String normalizeAndSortWords(String text) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return "";
        }
        List<String> words = Arrays.stream(normalized.split("\\s+"))
                .filter(w -> !w.isBlank())
                .sorted()
                .collect(Collectors.toList());
        return String.join(" ", words);
    }

    /**
     * Extrae los tokens (palabras) normalizados de un texto.
     */
    public static List<String> tokenize(String text) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(w -> !w.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una cadena de texto candidata contiene todos los tokens de la consulta,
     * sin importar el orden de las palabras ni las tildes/mayúsculas.
     */
    public static boolean matchesQueryTokens(String candidateText, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        if (candidateText == null || candidateText.isBlank()) {
            return false;
        }
        String normalizedCandidate = normalize(candidateText);
        List<String> queryTokens = tokenize(query);
        for (String token : queryTokens) {
            if (!normalizedCandidate.contains(token)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Escapa caracteres especiales de SQL LIKE (% y _) y barra invertida.
     */
    public static String escapeLikePattern(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Calcula la similitud de Levenshtein normalizada entre 0.0 y 1.0 (1.0 = idénticos).
     */
    public static double calculateSimilarity(String s1, String s2) {
        String norm1 = normalizeAndSortWords(s1);
        String norm2 = normalizeAndSortWords(s2);

        if (norm1.equals(norm2)) {
            return 1.0;
        }
        if (norm1.isEmpty() || norm2.isEmpty()) {
            return 0.0;
        }

        int distance = levenshteinDistance(norm1, norm2);
        int maxLen = Math.max(norm1.length(), norm2.length());
        return 1.0 - ((double) distance / maxLen);
    }

    private static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        for (int i = 0; i < len0; i++) cost[i] = i;

        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            for (int i = 1; i < len0; i++) {
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;
                int costReplace = cost[i - 1] + match;
                int costInsert = cost[i] + 1;
                int costDelete = newcost[i - 1] + 1;
                newcost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
            }
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        return cost[len0 - 1];
    }
}

