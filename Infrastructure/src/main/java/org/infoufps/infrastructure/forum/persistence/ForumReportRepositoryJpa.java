package org.infoufps.infrastructure.forum.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.forum.*;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA que implementa el contrato de Dominio {@link IForumReportRepository}.
 */
@ApplicationScoped
public class ForumReportRepositoryJpa implements IForumReportRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public ForumReport save(ForumReport report) {
        ForumReportJpa existing = entityManager.find(ForumReportJpa.class, report.getId().value());
        if (existing != null) {
            existing.setStatus(report.getStatus());
            existing.setResolvedAt(report.getResolvedAt());
            return report;
        }
        ForumReportJpa entity = toEntity(report);
        entityManager.persist(entity);
        return report;
    }

    @Override
    public Optional<ForumReport> findById(ReportId id) {
        if (id == null) {
            return Optional.empty();
        }
        ForumReportJpa entity = entityManager.find(ForumReportJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public boolean existsByReporterAndTarget(AccountId reporterId, ReportTargetType targetType, String targetId) {
        if (reporterId == null || targetType == null || targetId == null) {
            return false;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(r) FROM ForumReportJpa r WHERE r.reporterId = :reporterId AND r.targetType = :targetType AND r.targetId = :targetId",
                Long.class)
                .setParameter("reporterId", reporterId.value())
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .getSingleResult();
        return count != null && count > 0L;
    }

    @Override
    public long countPendingReportsByTarget(ReportTargetType targetType, String targetId) {
        if (targetType == null || targetId == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(r) FROM ForumReportJpa r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.status = :status",
                Long.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", ReportStatus.PENDING)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public List<ForumReport> findPendingReportsByTarget(ReportTargetType targetType, String targetId) {
        if (targetType == null || targetId == null) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                "SELECT r FROM ForumReportJpa r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.status = :status ORDER BY r.createdAt ASC",
                ForumReportJpa.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", ReportStatus.PENDING)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PagedResult<ForumReport> findAdmin(ReportStatus status, PageRequest pageRequest) {
        PageRequest request = pageRequest != null ? pageRequest : PageRequest.first();
        String countJpql = status != null
                ? "SELECT COUNT(r) FROM ForumReportJpa r WHERE r.status = :status"
                : "SELECT COUNT(r) FROM ForumReportJpa r";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        if (status != null) {
            countQuery.setParameter("status", status);
        }
        Long total = countQuery.getSingleResult();
        if (total == null || total == 0L) {
            return PagedResult.empty(request.page(), request.size());
        }

        String dataJpql = status != null
                ? "SELECT r FROM ForumReportJpa r WHERE r.status = :status ORDER BY r.createdAt DESC"
                : "SELECT r FROM ForumReportJpa r ORDER BY r.createdAt DESC";

        TypedQuery<ForumReportJpa> dataQuery = entityManager.createQuery(dataJpql, ForumReportJpa.class);
        if (status != null) {
            dataQuery.setParameter("status", status);
        }
        dataQuery.setFirstResult(request.getOffset());
        dataQuery.setMaxResults(request.size());

        List<ForumReport> items = dataQuery.getResultList().stream().map(this::toDomain).toList();
        return PagedResult.of(items, total, request);
    }

    // ── Mappers ──────────────────────────────────────────────────────────

    public ForumReportJpa toEntity(ForumReport domain) {
        ForumReportJpa entity = new ForumReportJpa();
        entity.setReportId(domain.getId().value());
        entity.setReporterId(domain.getReporterId().value());
        entity.setTargetType(domain.getTargetType());
        entity.setTargetId(domain.getTargetId());
        entity.setReason(domain.getReason());
        entity.setComment(domain.getComment());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setResolvedAt(domain.getResolvedAt());
        return entity;
    }

    public ForumReport toDomain(ForumReportJpa jpa) {
        return ForumReport.of(
                ReportId.of(jpa.getReportId()),
                AccountId.of(jpa.getReporterId()),
                jpa.getTargetType(),
                jpa.getTargetId(),
                jpa.getReason(),
                jpa.getComment(),
                jpa.getStatus(),
                jpa.getCreatedAt(),
                jpa.getResolvedAt()
        );
    }
}

