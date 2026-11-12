package org.infoufps.application.forum.dto;

import org.infoufps.domain.forum.ForumReport;

import java.time.LocalDateTime;

/**
 * DTO que representa un reporte de moderación para vistas administrativas.
 */
public record ForumReportDto(
        String id,
        String reporterId,
        String targetType,
        String targetId,
        String reason,
        String comment,
        String status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static ForumReportDto from(ForumReport report) {
        return new ForumReportDto(
                report.getId().value(),
                report.getReporterId().value(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason().name(),
                report.getComment(),
                report.getStatus().name(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}

