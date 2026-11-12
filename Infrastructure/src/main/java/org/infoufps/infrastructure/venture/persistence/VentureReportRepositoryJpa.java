package org.infoufps.infrastructure.venture.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA para {@link IVentureReportRepository}.
 */
@ApplicationScoped
@SuppressWarnings("JpaQlInspection")
public class VentureReportRepositoryJpa implements IVentureReportRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public VentureReport save(VentureReport report) {
        VentureReportJpa existing = entityManager.find(VentureReportJpa.class, report.getReportId());
        if (existing != null) {
            existing.setStatus(report.getStatus());
            existing.setResolvedAt(report.getResolvedAt());
            existing.setResolvedBy(report.getResolvedBy() != null ? report.getResolvedBy().value() : null);
            return report;
        }

        VentureReportJpa entity = toEntity(report);
        entityManager.persist(entity);
        return report;
    }

    @Override
    public Optional<VentureReport> findById(String reportId) {
        if (reportId == null || reportId.isBlank()) {
            return Optional.empty();
        }
        VentureReportJpa entity = entityManager.find(VentureReportJpa.class, reportId);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<VentureReport> findByReporterIdAndVentureId(AccountId reporterId, VentureId ventureId) {
        if (reporterId == null || ventureId == null) {
            return Optional.empty();
        }
        List<VentureReportJpa> results = entityManager.createQuery(
                "SELECT r FROM VentureReportJpa r WHERE r.reporterId = :reporterId AND r.ventureId = :ventureId",
                VentureReportJpa.class)
                .setParameter("reporterId", reporterId.value())
                .setParameter("ventureId", ventureId.value())
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(toDomain(results.get(0)));
    }

    @Override
    public long countPendingByVentureId(VentureId ventureId) {
        if (ventureId == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(r) FROM VentureReportJpa r WHERE r.ventureId = :ventureId AND r.status = :status",
                Long.class)
                .setParameter("ventureId", ventureId.value())
                .setParameter("status", ReportStatus.PENDING)
                .getSingleResult();

        return count != null ? count : 0L;
    }

    @Override
    public PageResult<VentureReport> findByStatus(ReportStatus status, PageRequest pageRequest) {
        PageRequest pr = pageRequest != null ? pageRequest : PageRequest.first();
        String countJpql = status != null
                ? "SELECT COUNT(r) FROM VentureReportJpa r WHERE r.status = :status"
                : "SELECT COUNT(r) FROM VentureReportJpa r";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        if (status != null) {
            countQuery.setParameter("status", status);
        }
        Long total = countQuery.getSingleResult();
        if (total == null || total == 0) {
            return PageResult.empty(pr.page(), pr.size());
        }

        String dataJpql = status != null
                ? "SELECT r FROM VentureReportJpa r WHERE r.status = :status ORDER BY r.createdAt DESC"
                : "SELECT r FROM VentureReportJpa r ORDER BY r.createdAt DESC";

        TypedQuery<VentureReportJpa> dataQuery = entityManager.createQuery(dataJpql, VentureReportJpa.class);
        if (status != null) {
            dataQuery.setParameter("status", status);
        }
        dataQuery.setFirstResult((int) pr.offset());
        dataQuery.setMaxResults(pr.size());

        List<VentureReport> reports = dataQuery.getResultList().stream().map(this::toDomain).toList();
        return PageResult.of(reports, total, pr);
    }

    private VentureReportJpa toEntity(VentureReport r) {
        VentureReportJpa entity = new VentureReportJpa();
        entity.setReportId(r.getReportId());
        entity.setReporterId(r.getReporterId().value());
        entity.setVentureId(r.getVentureId().value());
        entity.setReason(r.getReason());
        entity.setComment(r.getComment());
        entity.setStatus(r.getStatus());
        entity.setCreatedAt(r.getCreatedAt());
        entity.setResolvedAt(r.getResolvedAt());
        entity.setResolvedBy(r.getResolvedBy() != null ? r.getResolvedBy().value() : null);
        return entity;
    }

    private VentureReport toDomain(VentureReportJpa e) {
        return VentureReport.of(
                e.getReportId(),
                AccountId.of(e.getReporterId()),
                VentureId.of(e.getVentureId()),
                e.getReason(),
                e.getComment(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getResolvedAt(),
                e.getResolvedBy() != null ? AccountId.of(e.getResolvedBy()) : null
        );
    }
}

