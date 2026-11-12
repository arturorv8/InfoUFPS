package org.infoufps.infrastructure.forum.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA que implementa el contrato de Dominio {@link IForumThreadRepository}.
 */
@ApplicationScoped
public class ForumThreadRepositoryJpa implements IForumThreadRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public ForumThread save(ForumThread thread) {
        ForumThreadJpa existing = entityManager.find(ForumThreadJpa.class, thread.getId().value());
        if (existing != null) {
            existing.setTitle(thread.getTitle());
            existing.setBody(thread.getBody());
            existing.setStatus(thread.getStatus());
            existing.setUpdatedAt(thread.getUpdatedAt());
            return thread;
        }
        ForumThreadJpa entity = toEntity(thread);
        entityManager.persist(entity);
        return thread;
    }

    @Override
    public Optional<ForumThread> findById(ThreadId id) {
        if (id == null) {
            return Optional.empty();
        }
        ForumThreadJpa entity = entityManager.find(ForumThreadJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public PagedResult<ForumThread> findPublished(PageRequest pageRequest) {
        PageRequest request = pageRequest != null ? pageRequest : PageRequest.first();
        Long total = entityManager.createQuery(
                "SELECT COUNT(t) FROM ForumThreadJpa t WHERE t.status = :status", Long.class)
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();

        if (total == null || total == 0L) {
            return PagedResult.empty(request.page(), request.size());
        }

        List<ForumThread> items = entityManager.createQuery(
                "SELECT t FROM ForumThreadJpa t WHERE t.status = :status ORDER BY t.createdAt DESC",
                ForumThreadJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.size())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();

        return PagedResult.of(items, total, request);
    }

    @Override
    public PagedResult<ForumThread> findByAuthor(AccountId authorId, PageRequest pageRequest) {
        if (authorId == null) {
            return PagedResult.empty(1, 10);
        }
        PageRequest request = pageRequest != null ? pageRequest : PageRequest.first();
        Long total = entityManager.createQuery(
                "SELECT COUNT(t) FROM ForumThreadJpa t WHERE t.authorId = :authorId", Long.class)
                .setParameter("authorId", authorId.value())
                .getSingleResult();

        if (total == null || total == 0L) {
            return PagedResult.empty(request.page(), request.size());
        }

        List<ForumThread> items = entityManager.createQuery(
                "SELECT t FROM ForumThreadJpa t WHERE t.authorId = :authorId ORDER BY t.createdAt DESC",
                ForumThreadJpa.class)
                .setParameter("authorId", authorId.value())
                .setFirstResult(request.getOffset())
                .setMaxResults(request.size())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();

        return PagedResult.of(items, total, request);
    }

    @Override
    public PagedResult<ForumThread> findAdmin(Status status, PageRequest pageRequest) {
        PageRequest request = pageRequest != null ? pageRequest : PageRequest.first();
        String countJpql = status != null
                ? "SELECT COUNT(t) FROM ForumThreadJpa t WHERE t.status = :status"
                : "SELECT COUNT(t) FROM ForumThreadJpa t";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        if (status != null) {
            countQuery.setParameter("status", status);
        }
        Long total = countQuery.getSingleResult();
        if (total == null || total == 0L) {
            return PagedResult.empty(request.page(), request.size());
        }

        String dataJpql = status != null
                ? "SELECT t FROM ForumThreadJpa t WHERE t.status = :status ORDER BY t.createdAt DESC"
                : "SELECT t FROM ForumThreadJpa t ORDER BY t.createdAt DESC";

        TypedQuery<ForumThreadJpa> dataQuery = entityManager.createQuery(dataJpql, ForumThreadJpa.class);
        if (status != null) {
            dataQuery.setParameter("status", status);
        }
        dataQuery.setFirstResult(request.getOffset());
        dataQuery.setMaxResults(request.size());

        List<ForumThread> items = dataQuery.getResultList().stream().map(this::toDomain).toList();
        return PagedResult.of(items, total, request);
    }

    @Override
    public long countCreatedByAuthorSince(AccountId authorId, LocalDateTime since) {
        if (authorId == null || since == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM ForumThreadJpa t WHERE t.authorId = :authorId AND t.createdAt >= :since",
                Long.class)
                .setParameter("authorId", authorId.value())
                .setParameter("since", since)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(ThreadId id) {
        if (id == null) {
            return;
        }
        entityManager.createQuery("DELETE FROM ForumThreadJpa t WHERE t.threadId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    public boolean existsById(ThreadId id) {
        if (id == null) {
            return false;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM ForumThreadJpa t WHERE t.threadId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count != null && count > 0L;
    }

    // ── Mappers ──────────────────────────────────────────────────────────

    public ForumThreadJpa toEntity(ForumThread domain) {
        ForumThreadJpa entity = new ForumThreadJpa();
        entity.setThreadId(domain.getId().value());
        entity.setTitle(domain.getTitle());
        entity.setBody(domain.getBody());
        entity.setAuthorId(domain.getAuthorId().value());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public ForumThread toDomain(ForumThreadJpa jpa) {
        return ForumThread.of(
                ThreadId.of(jpa.getThreadId()),
                jpa.getTitle(),
                jpa.getBody(),
                AccountId.of(jpa.getAuthorId()),
                jpa.getStatus(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}

