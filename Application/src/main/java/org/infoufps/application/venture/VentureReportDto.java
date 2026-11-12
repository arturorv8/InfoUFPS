package org.infoufps.application.venture;

import org.infoufps.domain.venture.ReportReason;
import org.infoufps.domain.venture.ReportStatus;
import org.infoufps.domain.venture.VentureReport;

import java.time.LocalDateTime;

/**
 * DTO que representa un reporte de moderación para la administración.
 */
public record VentureReportDto(
        String reportId,
        String reporterId,
        String ventureId,
        ReportReason reason,
        String comment,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        String resolvedBy
) {
    public static VentureReportDto from(VentureReport report) {
        if (report == null) {
            return null;
        }
        return new VentureReportDto(
                report.getReportId(),
                report.getReporterId().value(),
                report.getVentureId().value(),
                report.getReason(),
                report.getComment(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getResolvedAt(),
                report.getResolvedBy() != null ? report.getResolvedBy().value() : null
        );
    }
}

