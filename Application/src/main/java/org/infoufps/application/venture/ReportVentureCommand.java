package org.infoufps.application.venture;

import org.infoufps.domain.venture.ReportReason;

/**
 * Comando para emitir un reporte de moderación sobre un emprendimiento.
 */
public record ReportVentureCommand(
        String ventureId,
        String reporterId,
        ReportReason reason,
        String comment
) {
}

