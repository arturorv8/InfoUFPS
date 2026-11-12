package org.infoufps.infrastructure.calendar.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.calendar.EventDuplicateCriteria;
import org.infoufps.domain.calendar.EventPageCriteria;
import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.domain.calendar.ModerationCriteria;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.Status;
import org.infoufps.infrastructure.calendar.persistence.EventJpa;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Adaptador JPA del puerto {@link IEventRepository}.
 */
@ApplicationScoped
public class EventRepositoryJpa implements IEventRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Event save(Event event) {
        EventJpa existing = entityManager.find(EventJpa.class, event.getId().value());
        if (existing != null) {
            updateJpa(existing, event);
            return event;
        }
        EventJpa entity = EventJpaMapper.toEntity(event);
        entityManager.persist(entity);
        return event;
    }

    private void updateJpa(EventJpa jpa, Event domain) {
        jpa.setTitle(domain.getTitle());
        jpa.setType(domain.getType());
        jpa.setLocation(domain.getLocation());
        jpa.setStartDate(domain.getStartDate());
        jpa.setEndDate(domain.getEndDate());
        jpa.setStartTime(domain.getStartTime());
        jpa.setEndTime(domain.getEndTime());
        jpa.setDescription(domain.getDescription());
        jpa.setInfoUrl(domain.getInfoUrl());
        jpa.setStatus(domain.getStatus());
        jpa.setProposedBy(domain.getProposedBy().value());
        jpa.setCreatedByAdmin(domain.isCreatedByAdmin());
        jpa.setPublishedAt(domain.getPublishedAt());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setRevision(domain.getRevision());
    }

    @Override
    public Optional<Event> findById(EventId id) {
        EventJpa entity = entityManager.find(EventJpa.class, id.value());
        return Optional.ofNullable(entity).map(EventJpaMapper::toDomain);
    }

    @Override
    public List<Event> findPublishedOverlapping(EventRangeCriteria criteria) {
        StringBuilder jpql = new StringBuilder(
                "SELECT e FROM EventJpa e " +
                        "WHERE e.status = :status AND e.startDate <= :to AND e.endDate >= :from");
        if (criteria.hasTypes()) {
            jpql.append(" AND e.type IN :types");
        }
        jpql.append(" ORDER BY e.startDate ASC, CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, ")
                .append("e.startTime ASC, LOWER(e.title) ASC");

        TypedQuery<EventJpa> query = entityManager.createQuery(jpql.toString(), EventJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("from", criteria.from())
                .setParameter("to", criteria.to());
        if (criteria.hasTypes()) {
            query.setParameter("types", criteria.types());
        }
        return query.getResultList().stream().map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public PageResult<Event> findPublishedUpcomingOrOngoing(EventPageCriteria criteria) {
        String base = " FROM EventJpa e WHERE e.status = :status AND e.endDate >= :today";
        String typeFilter = criteria.hasTypes() ? " AND e.type IN :types" : "";

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(e)" + base + typeFilter, Long.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("today", criteria.today());
        if (criteria.hasTypes()) {
            countQuery.setParameter("types", criteria.types());
        }
        Long total = countQuery.getSingleResult();

        TypedQuery<EventJpa> dataQuery = entityManager.createQuery(
                        "SELECT e" + base + typeFilter +
                                " ORDER BY e.startDate ASC, CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, " +
                                "e.startTime ASC, LOWER(e.title) ASC",
                        EventJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("today", criteria.today())
                .setFirstResult((int) criteria.pageRequest().offset())
                .setMaxResults(criteria.pageRequest().size());
        if (criteria.hasTypes()) {
            dataQuery.setParameter("types", criteria.types());
        }

        List<Event> events = dataQuery.getResultList().stream().map(EventJpaMapper::toDomain).toList();
        return PageResult.of(events, total != null ? total : 0L, criteria.pageRequest());
    }

    @Override
    public PageResult<Event> findByProposedBy(AccountId accountId, PageRequest pageRequest) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.proposedBy = :account", Long.class)
                .setParameter("account", accountId.value())
                .getSingleResult();

        List<Event> events = entityManager.createQuery(
                        "SELECT e FROM EventJpa e WHERE e.proposedBy = :account ORDER BY e.createdAt DESC",
                        EventJpa.class)
                .setParameter("account", accountId.value())
                .setFirstResult((int) pageRequest.offset())
                .setMaxResults(pageRequest.size())
                .getResultList()
                .stream()
                .map(EventJpaMapper::toDomain)
                .toList();

        return PageResult.of(events, total != null ? total : 0L, pageRequest);
    }

    @Override
    public PageResult<Event> findByStatus(ModerationCriteria criteria) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.status = :status", Long.class)
                .setParameter("status", criteria.status())
                .getSingleResult();

        List<Event> events = entityManager.createQuery(
                        "SELECT e FROM EventJpa e WHERE e.status = :status ORDER BY e.createdAt ASC",
                        EventJpa.class)
                .setParameter("status", criteria.status())
                .setFirstResult((int) criteria.pageRequest().offset())
                .setMaxResults(criteria.pageRequest().size())
                .getResultList()
                .stream()
                .map(EventJpaMapper::toDomain)
                .toList();

        return PageResult.of(events, total != null ? total : 0L, criteria.pageRequest());
    }

    @Override
    public List<Event> findDuplicates(EventDuplicateCriteria criteria) {
        StringBuilder jpql = new StringBuilder(
                "SELECT e FROM EventJpa e " +
                        "WHERE LOWER(e.title) = :title AND e.startDate = :startDate " +
                        "AND e.status <> :archived");
        if (criteria.startTime() == null) {
            jpql.append(" AND e.startTime IS NULL");
        } else {
            jpql.append(" AND e.startTime = :startTime");
        }
        if (criteria.excludeId() != null) {
            jpql.append(" AND e.eventId <> :excludeId");
        }
        jpql.append(" ORDER BY e.createdAt ASC");

        TypedQuery<EventJpa> query = entityManager.createQuery(jpql.toString(), EventJpa.class)
                .setParameter("title", criteria.title().toLowerCase(Locale.ROOT))
                .setParameter("startDate", criteria.startDate())
                .setParameter("archived", Status.ARCHIVED);
        if (criteria.startTime() != null) {
            query.setParameter("startTime", criteria.startTime());
        }
        if (criteria.excludeId() != null) {
            query.setParameter("excludeId", criteria.excludeId().value());
        }
        return query.getResultList().stream().map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public long countProposalsByAccountOnDate(AccountId accountId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        Long count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e " +
                                "WHERE e.proposedBy = :account AND e.createdAt >= :from AND e.createdAt < :to",
                        Long.class)
                .setParameter("account", accountId.value())
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(EventId id) {
        entityManager.createQuery("DELETE FROM EventJpa e WHERE e.eventId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    public boolean existsById(EventId id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.eventId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count != null && count > 0;
    }
}
