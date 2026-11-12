package org.infoufps.domain.venture;

/**
 * Resumen agregado de calificaciones de un emprendimiento (promedio y total).
 *
 * @param average Promedio de estrellas (0.0 si no tiene calificaciones).
 * @param total   Número total de calificaciones registradas.
 */
public record RatingSummary(double average, long total) {

    public static RatingSummary empty() {
        return new RatingSummary(0.0, 0L);
    }

    public static RatingSummary of(Double average, Long total) {
        double avg = (average != null && !average.isNaN()) ? Math.round(average * 10.0) / 10.0 : 0.0;
        long tot = (total != null && total >= 0) ? total : 0L;
        return new RatingSummary(avg, tot);
    }
}

