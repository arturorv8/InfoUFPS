package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que modela un reporte de moderación sobre un hilo o comentario del foro.
 * <p>
 * Reglas de negocio:
 * - Es único por (reporterId, targetType, targetId).
 * - No se permite reportar contenido propio.
 * - Los reportes nacen con estado {@link ReportStatus#PENDING}.
 * - Al ser resueltos por un administrador pasan a {@link ReportStatus#RESOLVED}.
 */
public class ForumReport {

    public static final int MAX_COMMENT_LENGTH = 500;

    private final ReportId id;
    private final AccountId reporterId;
    private final ReportTargetType targetType;
    private final String targetId;
    private final ReportReason reason;
    private final String comment;
    private ReportStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    private ForumReport(ReportId id,
                        AccountId reporterId,
                        ReportTargetType targetType,
                        String targetId,
                        ReportReason reason,
                        String comment,
                        ReportStatus status,
                        LocalDateTime createdAt,
                        LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id, "El id del reporte es obligatorio");
        this.reporterId = Objects.requireNonNull(reporterId, "El reporterId es obligatorio");
        this.targetType = Objects.requireNonNull(targetType, "El targetType es obligatorio");
        this.targetId = validateTargetId(targetId);
        this.reason = Objects.requireNonNull(reason, "El motivo del reporte es obligatorio");
        this.comment = validateComment(comment);
        this.status = status != null ? status : ReportStatus.PENDING;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.resolvedAt = resolvedAt;
    }

    public static ForumReport create(AccountId reporterId,
                                    ReportTargetType targetType,
                                    String targetId,
                                    ReportReason reason,
                                    String comment) {
        return new ForumReport(
                ReportId.generate(),
                reporterId,
                targetType,
                targetId,
                reason,
                comment,
                ReportStatus.PENDING,
                BogotaTime.now(),
                null
        );
    }

    public static ForumReport of(ReportId id,
                                 AccountId reporterId,
                                 ReportTargetType targetType,
                                 String targetId,
                                 ReportReason reason,
                                 String comment,
                                 ReportStatus status,
                                 LocalDateTime createdAt,
                                 LocalDateTime resolvedAt) {
        return new ForumReport(id, reporterId, targetType, targetId, reason, comment, status, createdAt, resolvedAt);
    }

    /**
     * Resuelve el reporte marcándolo como resuelto y asignando la fecha de resolución.
     */
    public void resolve() {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = BogotaTime.now();
    }

    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }

    private static String validateTargetId(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El identificador del recurso reportado es obligatorio");
        }
        return targetId.strip();
    }

    private static String validateComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        String clean = comment.strip();
        if (clean.length() > MAX_COMMENT_LENGTH) {
            throw new DomainException("El comentario del reporte no puede exceder los " + MAX_COMMENT_LENGTH + " caracteres");
        }
        return clean;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public ReportId getId() { return id; }
    public AccountId getReporterId() { return reporterId; }
    public ReportTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public ReportReason getReason() { return reason; }
    public String getComment() { return comment; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}

