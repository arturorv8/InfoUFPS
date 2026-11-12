package org.infoufps.infrastructure.calendar.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.FavoriteEvent;
import org.infoufps.domain.calendar.Status;
import org.infoufps.infrastructure.calendar.persistence.EventJpa;
import org.infoufps.infrastructure.calendar.persistence.FavoriteEventIdJpa;
import org.infoufps.infrastructure.calendar.persistence.FavoriteEventJpa;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA del puerto {@link IFavoriteEventRepository}.
 * Solo expone favoritos cuyos eventos siguen publicados.
 */
@ApplicationScoped
public class FavoriteEventRepositoryJpa implements IFavoriteEventRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public FavoriteEvent save(FavoriteEvent favorite) {
        FavoriteEventJpa entity = new FavoriteEventJpa();
        entity.setId(new FavoriteEventIdJpa(
                favorite.getAccountId().value(),
                favorite.getEventId().value()));
        entity.setCreatedAt(favorite.getCreatedAt());
        entityManager.merge(entity);
        return favorite;
    }

    @Override
    public Optional<FavoriteEvent> findById(AccountId accountId, EventId eventId) {
        FavoriteEventJpa entity = entityManager.find(
                FavoriteEventJpa.class,
                new FavoriteEventIdJpa(accountId.value(), eventId.value()));
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(FavoriteEvent.of(
                AccountId.of(entity.getId().getAccountId()),
                EventId.of(entity.getId().getEventId()),
                entity.getCreatedAt()));
    }

    @Override
    public boolean exists(AccountId accountId, EventId eventId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(f) FROM FavoriteEventJpa f " +
                                "WHERE f.id.accountId = :account AND f.id.eventId = :event",
                        Long.class)
                .setParameter("account", accountId.value())
                .setParameter("event", eventId.value())
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void delete(AccountId accountId, EventId eventId) {
        entityManager.createQuery(
                        "DELETE FROM FavoriteEventJpa f " +
                                "WHERE f.id.accountId = :account AND f.id.eventId = :event")
                .setParameter("account", accountId.value())
                .setParameter("event", eventId.value())
                .executeUpdate();
    }

    @Override
    public List<EventId> findPublishedEventIdsByAccount(AccountId accountId) {
        return entityManager.createQuery(
                        "SELECT f.id.eventId FROM FavoriteEventJpa f, EventJpa e " +
                                "WHERE f.id.eventId = e.eventId AND f.id.accountId = :account " +
                                "AND e.status = :status " +
                                "ORDER BY e.startDate ASC, e.startTime ASC, LOWER(e.title) ASC",
                        String.class)
                .setParameter("account", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .getResultList()
                .stream()
                .map(EventId::of)
                .toList();
    }

    @Override
    public PageResult<Event> findPublishedFavoriteEvents(AccountId accountId, PageRequest pageRequest) {
        String base = " FROM FavoriteEventJpa f, EventJpa e " +
                "WHERE f.id.eventId = e.eventId AND f.id.accountId = :account AND e.status = :status";

        Long total = entityManager.createQuery("SELECT COUNT(e)" + base, Long.class)
                .setParameter("account", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();

        TypedQuery<EventJpa> query = entityManager.createQuery(
                        "SELECT e" + base +
                                " ORDER BY e.startDate ASC, CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, " +
                                "e.startTime ASC, LOWER(e.title) ASC",
                        EventJpa.class)
                .setParameter("account", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .setFirstResult((int) pageRequest.offset())
                .setMaxResults(pageRequest.size());

        List<Event> events = query.getResultList().stream().map(EventJpaMapper::toDomain).toList();
        return PageResult.of(events, total != null ? total : 0L, pageRequest);
    }

    @Override
    public long countPublishedByAccount(AccountId accountId) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(e) FROM FavoriteEventJpa f, EventJpa e " +
                                "WHERE f.id.eventId = e.eventId AND f.id.accountId = :account " +
                                "AND e.status = :status",
                        Long.class)
                .setParameter("account", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();
        return total != null ? total : 0L;
    }
}
