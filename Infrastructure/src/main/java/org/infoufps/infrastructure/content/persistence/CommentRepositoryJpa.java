package org.infoufps.infrastructure.content.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentId;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ContentId;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA que implementa el contrato de Dominio {@link ICommentRepository}.
 * <p>
 * Gestiona el ciclo de vida de persistencia en la base de datos relacional para la entidad polimórfica {@link Comment},
 * transformando los datos entre el Modelo de Dominio y la Entidad de Persistencia {@link CommentJpa}.
 */
@ApplicationScoped
public class CommentRepositoryJpa implements ICommentRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Comment save(Comment comment) {
        CommentJpa existing = entityManager.find(CommentJpa.class, comment.getId().value());
        if (existing != null) {
            existing.setText(comment.getText());
            existing.setStatus(comment.getStatus());
            existing.setUpdatedAt(comment.getUpdatedAt());
            return comment;
        }
        CommentJpa entity = toEntity(comment);
        entityManager.persist(entity);
        return comment;
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        CommentJpa entity = entityManager.find(CommentJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Comment> findByContentId(ContentId contentId) {
        return findPublishedByTarget(CommentTargetType.CONTENT, contentId.value());
    }

    @Override
    public List<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.targetType = :targetType AND c.targetId = :targetId AND c.status = :status " +
                                "ORDER BY c.createdAt ASC",
                        CommentJpa.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", Status.PUBLISHED)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId, PageRequest pageRequest) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c " +
                                "WHERE c.targetType = :targetType AND c.targetId = :targetId AND c.status = :status",
                        Long.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();

        PageRequest request = pageRequest != null ? pageRequest : PageRequest.first();
        List<Comment> items = entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.targetType = :targetType AND c.targetId = :targetId AND c.status = :status " +
                                "ORDER BY c.createdAt ASC",
                        CommentJpa.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", Status.PUBLISHED)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.size())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();

        return PagedResult.of(items, total != null ? total : 0L, request);
    }

    @Override
    public List<Comment> findByAuthorId(AccountId authorId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.authorId = :authorId " +
                                "ORDER BY c.createdAt DESC",
                        CommentJpa.class)
                .setParameter("authorId", authorId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.targetType = :targetType AND c.targetId = :targetId AND c.authorId = :authorId AND c.status = :status " +
                                "ORDER BY c.createdAt ASC",
                        CommentJpa.class)
                .setParameter("targetType", CommentTargetType.CONTENT)
                .setParameter("targetId", contentId.value())
                .setParameter("authorId", authorId.value())
                .setParameter("status", Status.PUBLISHED)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByContentId(ContentId contentId) {
        return countPublishedByTarget(CommentTargetType.CONTENT, contentId.value());
    }

    @Override
    public long countPublishedByTarget(CommentTargetType targetType, String targetId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c " +
                                "WHERE c.targetType = :targetType AND c.targetId = :targetId AND c.status = :status",
                        Long.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public long countByAuthorIdSince(AccountId authorId, LocalDateTime since) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c " +
                                "WHERE c.authorId = :authorId AND c.createdAt >= :since",
                        Long.class)
                .setParameter("authorId", authorId.value())
                .setParameter("since", since)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(CommentId id) {
        entityManager.createQuery(
                        "DELETE FROM CommentJpa c WHERE c.commentId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    public boolean existsById(CommentId id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c WHERE c.commentId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count != null && count > 0;
    }

    // ── Mapeo: Dominio → JPA Entity ──────────────────────────────────

    public CommentJpa toEntity(Comment domain) {
        CommentJpa entity = new CommentJpa();
        entity.setCommentId(domain.getId().value());
        entity.setTargetType(domain.getTargetType());
        entity.setTargetId(domain.getTargetId());
        entity.setAuthorId(domain.getAuthorId().value());
        entity.setText(domain.getText());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    // ── Mapeo: JPA Entity → Dominio ──────────────────────────────────

    public Comment toDomain(CommentJpa jpa) {
        return Comment.of(
                CommentId.of(jpa.getCommentId()),
                jpa.getTargetType(),
                jpa.getTargetId(),
                AccountId.of(jpa.getAuthorId()),
                jpa.getText(),
                jpa.getStatus(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}
