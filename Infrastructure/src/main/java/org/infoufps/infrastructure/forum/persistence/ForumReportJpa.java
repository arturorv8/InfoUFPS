package org.infoufps.infrastructure.forum.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.forum.ReportReason;
import org.infoufps.domain.forum.ReportStatus;
import org.infoufps.domain.forum.ReportTargetType;

import java.time.LocalDateTime;

/**
 * Entidad de Persistencia JPA para la tabla 'forum_reports'.
 */
@Entity
@Table(name = "forum_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_forum_reports_reporter_target", columnNames = {"reporter_id", "target_type", "target_id"})
        },
        indexes = {
                @Index(name = "idx_forum_reports_target_status", columnList = "target_type, target_id, status"),
                @Index(name = "idx_forum_reports_status", columnList = "status")
        })
public class ForumReportJpa {

    @Id
    @Column(name = "report_id", length = 36, nullable = false)
    private String reportId;

    @Column(name = "reporter_id", length = 36, nullable = false)
    private String reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private ReportTargetType targetType;

    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 50, nullable = false)
    private ReportReason reason;

    @Column(name = "comment", length = 500)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ForumReportJpa() {
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public ReportTargetType getTargetType() { return targetType; }
    public void setTargetType(ReportTargetType targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

