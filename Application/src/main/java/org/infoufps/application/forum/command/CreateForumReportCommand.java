package org.infoufps.application.forum.command;

import org.infoufps.domain.forum.ReportReason;
import org.infoufps.domain.forum.ReportTargetType;

public record CreateForumReportCommand(
        String reporterId,
        ReportTargetType targetType,
        String targetId,
        ReportReason reason,
        String comment
) {
}

