package org.infoufps.domain.academic;

/**
 * Resumen agregado de calificaciones publicadas de un profesor o materia.
 *
 * @param average promedio de calificaciones; {@code null} cuando no hay reseñas.
 * @param total   cantidad de reseñas publicadas.
 */
public record RatingSummary(Double average, long total) {

    public static RatingSummary empty() {
        return new RatingSummary(null, 0L);
    }
}
