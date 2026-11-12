package org.infoufps.api.venture.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.infoufps.domain.venture.ReportReason;

/**
 * Petición para reportar un emprendimiento para moderación.
 */
public record ReportVentureRequest(
        @NotNull(message = "El motivo de reporte es obligatorio")
        ReportReason reason,

        @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
        String comment
) {
}

