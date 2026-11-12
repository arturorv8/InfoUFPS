package org.infoufps.infrastructure.venture.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.venture.ReportReason;
import org.infoufps.domain.venture.ReportStatus;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'venture_reports'.
 */
@Entity
@Table(name = "venture_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_venture_reports_reporter_venture", columnNames = {"reporter_id", "venture_id"})
        },
        indexes = {
                @Index(name = "idx_venture_reports_venture_status", columnList = "venture_id, status"),
                @Index(name = "idx_venture_reports_status", columnList = "status")
        })
public class VentureReportJpa {

    @Id
    @Column(name = "report_id", length = 36, nullable = false)
    private String reportId;

    @Column(name = "reporter_id", length = 36, nullable = false)
    private String reporterId;

    @Column(name = "venture_id", length = 36, nullable = false)
    private String ventureId;

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

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    public VentureReportJpa() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getVentureId() { return ventureId; }
    public void setVentureId(String ventureId) { this.ventureId = ventureId; }

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

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
}

