package org.infoufps.api.venture.dto;

import jakarta.validation.constraints.NotNull;
import org.infoufps.application.venture.ResolveAction;

/**
 * Petición para resolver un reporte de moderación por parte de un administrador.
 */
public record ResolveReportRequest(
        @NotNull(message = "La acción de resolución es obligatoria")
        ResolveAction action
) {
}

