package org.infoufps.infrastructure.venture.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.*;

/**
 * Adaptador de Persistencia JPA para {@link IFavoriteVentureRepository}.
 */
@ApplicationScoped
public class FavoriteVentureRepositoryJpa implements IFavoriteVentureRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public FavoriteVenture save(FavoriteVenture favorite) {
        FavoriteVentureIdJpa id = new FavoriteVentureIdJpa(
                favorite.getAccountId().value(),
                favorite.getVentureId().value()
        );
        FavoriteVentureJpa existing = entityManager.find(FavoriteVentureJpa.class, id);
        if (existing != null) {
            return favorite; // Idempotente
        }

        FavoriteVentureJpa entity = new FavoriteVentureJpa(id, favorite.getCreatedAt());
        entityManager.persist(entity);
        return favorite;
    }

    @Override
    public boolean existsByAccountIdAndVentureId(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) {
            return false;
        }
        FavoriteVentureIdJpa id = new FavoriteVentureIdJpa(accountId.value(), ventureId.value());
        return entityManager.find(FavoriteVentureJpa.class, id) != null;
    }

    @Override
    @Transactional
    public void delete(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) {
            return;
        }
        FavoriteVentureIdJpa id = new FavoriteVentureIdJpa(accountId.value(), ventureId.value());
        FavoriteVentureJpa entity = entityManager.find(FavoriteVentureJpa.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<VentureId> findFavoriteVentureIdsByAccountId(AccountId accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        List<String> ids = entityManager.createQuery(
                "SELECT f.id.ventureId FROM FavoriteVentureJpa f WHERE f.id.accountId = :accountId", String.class)
                .setParameter("accountId", accountId.value())
                .getResultList();

        return ids.stream().map(VentureId::of).toList();
    }

    @Override
    public PageResult<Venture> findFavoriteVenturesByAccountId(AccountId accountId, PageRequest pageRequest) {
        if (accountId == null) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }
        PageRequest pr = pageRequest != null ? pageRequest : PageRequest.first();

        // Solo se listan los que actualmente están PUBLISHED
        String countJpql = "SELECT COUNT(f) FROM FavoriteVentureJpa f, VentureJpa v " +
                "WHERE f.id.ventureId = v.ventureId AND f.id.accountId = :accountId AND v.status = :status";

        Long total = entityManager.createQuery(countJpql, Long.class)
                .setParameter("accountId", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .getSingleResult();

        if (total == null || total == 0) {
            return PageResult.empty(pr.page(), pr.size());
        }

        String dataJpql = "SELECT v FROM FavoriteVentureJpa f, VentureJpa v " +
                "WHERE f.id.ventureId = v.ventureId AND f.id.accountId = :accountId AND v.status = :status " +
                "ORDER BY f.createdAt DESC";

        List<VentureJpa> entities = entityManager.createQuery(dataJpql, VentureJpa.class)
                .setParameter("accountId", accountId.value())
                .setParameter("status", Status.PUBLISHED)
                .setFirstResult((int) pr.offset())
                .setMaxResults(pr.size())
                .getResultList();

        List<Venture> ventures = entities.stream().map(this::toDomain).toList();
        return PageResult.of(ventures, total, pr);
    }

    private Venture toDomain(VentureJpa e) {
        Contact contact = Contact.of(e.getContactType(), e.getContactValue());
        Set<ServiceMode> modes = e.getServiceModes() != null && !e.getServiceModes().isEmpty()
                ? EnumSet.copyOf(e.getServiceModes())
                : Collections.emptySet();

        return Venture.of(
                VentureId.of(e.getVentureId()),
                e.getName(),
                e.getDescription(),
                e.getCategory(),
                AccountId.of(e.getOwnerId()),
                contact,
                modes,
                e.getLocation(),
                e.isActive(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getPublishedAt(),
                e.getNameSearch()
        );
    }
}

