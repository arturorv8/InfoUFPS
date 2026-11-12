package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio que representa un reporte de moderación sobre un emprendimiento.
 */
public class VentureReport {

    public static final int MAX_COMMENT_LENGTH = 500;

    private final String reportId;
    private final AccountId reporterId;
    private final VentureId ventureId;
    private final ReportReason reason;
    private final String comment;
    private ReportStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private AccountId resolvedBy;

    private VentureReport(String reportId,
                          AccountId reporterId,
                          VentureId ventureId,
                          ReportReason reason,
                          String comment,
                          ReportStatus status,
                          LocalDateTime createdAt,
                          LocalDateTime resolvedAt,
                          AccountId resolvedBy) {
        this.reportId = Objects.requireNonNull(reportId, "El id del reporte es obligatorio");
        this.reporterId = Objects.requireNonNull(reporterId, "El id del reportante es obligatorio");
        this.ventureId = Objects.requireNonNull(ventureId, "El id del emprendimiento es obligatorio");
        this.reason = Objects.requireNonNull(reason, "El motivo del reporte es obligatorio");
        this.comment = validateComment(comment);
        this.status = Objects.requireNonNull(status, "El estado del reporte es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    /**
     * Fábrica para crear un nuevo reporte. Inicia en estado PENDING.
     */
    public static VentureReport create(AccountId reporterId,
                                       VentureId ventureId,
                                       ReportReason reason,
                                       String comment) {
        return new VentureReport(
                UUID.randomUUID().toString(),
                reporterId,
                ventureId,
                reason,
                comment,
                ReportStatus.PENDING,
                BogotaTime.now(),
                null,
                null
        );
    }

    /**
     * Fábrica para reconstruir desde persistencia.
     */
    public static VentureReport of(String reportId,
                                   AccountId reporterId,
                                   VentureId ventureId,
                                   ReportReason reason,
                                   String comment,
                                   ReportStatus status,
                                   LocalDateTime createdAt,
                                   LocalDateTime resolvedAt,
                                   AccountId resolvedBy) {
        return new VentureReport(
                reportId,
                reporterId,
                ventureId,
                reason,
                comment,
                status,
                createdAt,
                resolvedAt,
                resolvedBy
        );
    }

    private static String validateComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        String trimmed = comment.strip();
        if (trimmed.length() > MAX_COMMENT_LENGTH) {
            throw new DomainException("El comentario del reporte no puede superar los " + MAX_COMMENT_LENGTH + " caracteres");
        }
        return trimmed;
    }

    /**
     * Marca el reporte como resuelto por un administrador.
     */
    public void resolve(AccountId adminId) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = BogotaTime.now();
        this.resolvedBy = Objects.requireNonNull(adminId, "El id del administrador resolutor es obligatorio");
    }

    public String getReportId() { return reportId; }
    public AccountId getReporterId() { return reporterId; }
    public VentureId getVentureId() { return ventureId; }
    public ReportReason getReason() { return reason; }
    public String getComment() { return comment; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public AccountId getResolvedBy() { return resolvedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VentureReport that)) return false;
        return Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
}

