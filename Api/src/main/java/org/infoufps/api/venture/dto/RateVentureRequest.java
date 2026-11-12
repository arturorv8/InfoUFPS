package org.infoufps.api.venture.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Petición para calificar un emprendimiento con un puntaje de 1 a 5 estrellas.
 */
public record RateVentureRequest(
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        int score
) {
}

