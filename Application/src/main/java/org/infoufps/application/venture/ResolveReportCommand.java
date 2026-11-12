package org.infoufps.application.venture;

/**
 * Comando para resolver un reporte por parte del administrador.
 */
public record ResolveReportCommand(
        String reportId,
        String adminId,
        ResolveAction action
) {
}

