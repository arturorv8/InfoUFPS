package org.infoufps.infrastructure.forum.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.forum.ForumLike;
import org.infoufps.domain.forum.IForumLikeRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.time.BogotaTime;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adaptador de Persistencia JPA que implementa el contrato de Dominio {@link IForumLikeRepository}.
 */
@ApplicationScoped
public class ForumLikeRepositoryJpa implements IForumLikeRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public boolean addLike(AccountId accountId, ThreadId threadId) {
        if (accountId == null || threadId == null) {
            return false;
        }
        ForumLikeIdJpa id = new ForumLikeIdJpa(accountId.value(), threadId.value());
        ForumLikeJpa existing = entityManager.find(ForumLikeJpa.class, id);
        if (existing != null) {
            return false;
        }
        ForumLikeJpa entity = new ForumLikeJpa(id, BogotaTime.now());
        entityManager.persist(entity);
        return true;
    }

    @Override
    @Transactional
    public boolean removeLike(AccountId accountId, ThreadId threadId) {
        if (accountId == null || threadId == null) {
            return false;
        }
        ForumLikeIdJpa id = new ForumLikeIdJpa(accountId.value(), threadId.value());
        ForumLikeJpa existing = entityManager.find(ForumLikeJpa.class, id);
        if (existing != null) {
            entityManager.remove(existing);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByAccountAndThread(AccountId accountId, ThreadId threadId) {
        if (accountId == null || threadId == null) {
            return false;
        }
        ForumLikeIdJpa id = new ForumLikeIdJpa(accountId.value(), threadId.value());
        return entityManager.find(ForumLikeJpa.class, id) != null;
    }

    @Override
    public long countByThreadId(ThreadId threadId) {
        if (threadId == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(l) FROM ForumLikeJpa l WHERE l.id.threadId = :threadId", Long.class)
                .setParameter("threadId", threadId.value())
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public Set<String> findLikedThreadIdsByAccount(AccountId accountId) {
        if (accountId == null) {
            return Collections.emptySet();
        }
        List<String> list = entityManager.createQuery(
                "SELECT l.id.threadId FROM ForumLikeJpa l WHERE l.id.accountId = :accountId", String.class)
                .setParameter("accountId", accountId.value())
                .getResultList();
        return new HashSet<>(list);
    }

    // ── Mappers ──────────────────────────────────────────────────────────

    public ForumLikeJpa toEntity(ForumLike domain) {
        ForumLikeIdJpa id = new ForumLikeIdJpa(domain.getAccountId().value(), domain.getThreadId().value());
        return new ForumLikeJpa(id, domain.getCreatedAt());
    }

    public ForumLike toDomain(ForumLikeJpa jpa) {
        return ForumLike.of(
                AccountId.of(jpa.getId().getAccountId()),
                ThreadId.of(jpa.getId().getThreadId()),
                jpa.getCreatedAt()
        );
    }
}

