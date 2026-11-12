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
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.venture.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Adaptador de Persistencia JPA para {@link IVentureRepository}.
 */
@ApplicationScoped
@SuppressWarnings("JpaQlInspection")
public class VentureRepositoryJpa implements IVentureRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Venture save(Venture venture) {
        VentureJpa existing = entityManager.find(VentureJpa.class, venture.getId().value());
        if (existing != null) {
            existing.setName(venture.getName());
            existing.setDescription(venture.getDescription());
            existing.setCategory(venture.getCategory());
            existing.setContactType(venture.getContact().type());
            existing.setContactValue(venture.getContact().value());
            existing.setServiceModes(new HashSet<>(venture.getServiceModes()));
            existing.setLocation(venture.getLocation());
            existing.setActive(venture.isActive());
            existing.setStatus(venture.getStatus());
            existing.setUpdatedAt(venture.getUpdatedAt());
            existing.setPublishedAt(venture.getPublishedAt());
            existing.setNameSearch(venture.getNameSearch());
            return venture;
        }

        VentureJpa entity = toEntity(venture);
        entityManager.persist(entity);
        return venture;
    }

    @Override
    public Optional<Venture> findById(VentureId id) {
        if (id == null) {
            return Optional.empty();
        }
        VentureJpa entity = entityManager.find(VentureJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public boolean existsByOwnerIdAndNameSearch(AccountId ownerId, String nameSearch, VentureId excludeId) {
        if (ownerId == null || nameSearch == null || nameSearch.isBlank()) {
            return false;
        }
        String jpql = excludeId != null
                ? "SELECT COUNT(v) FROM VentureJpa v WHERE v.ownerId = :ownerId AND v.nameSearch = :nameSearch AND v.ventureId <> :excludeId"
                : "SELECT COUNT(v) FROM VentureJpa v WHERE v.ownerId = :ownerId AND v.nameSearch = :nameSearch";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class)
                .setParameter("ownerId", ownerId.value())
                .setParameter("nameSearch", nameSearch);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId.value());
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNameSearchAndDifferentOwner(String nameSearch, AccountId ownerId) {
        if (nameSearch == null || nameSearch.isBlank()) {
            return false;
        }
        String jpql = ownerId != null
                ? "SELECT COUNT(v) FROM VentureJpa v WHERE v.nameSearch = :nameSearch AND v.ownerId <> :ownerId"
                : "SELECT COUNT(v) FROM VentureJpa v WHERE v.nameSearch = :nameSearch";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class)
                .setParameter("nameSearch", nameSearch);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId.value());
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByContactValueAndDifferentOwner(String contactValue, AccountId ownerId) {
        if (contactValue == null || contactValue.isBlank()) {
            return false;
        }
        String jpql = ownerId != null
                ? "SELECT COUNT(v) FROM VentureJpa v WHERE v.contactValue = :contactValue AND v.ownerId <> :ownerId"
                : "SELECT COUNT(v) FROM VentureJpa v WHERE v.contactValue = :contactValue";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class)
                .setParameter("contactValue", contactValue);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId.value());
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public long countByOwnerId(AccountId ownerId) {
        if (ownerId == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(v) FROM VentureJpa v WHERE v.ownerId = :ownerId", Long.class)
                .setParameter("ownerId", ownerId.value())
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public long countCreatedByOwnerIdSince(AccountId ownerId, LocalDateTime since) {
        if (ownerId == null || since == null) {
            return 0L;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(v) FROM VentureJpa v WHERE v.ownerId = :ownerId AND v.createdAt >= :since", Long.class)
                .setParameter("ownerId", ownerId.value())
                .setParameter("since", since)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public PageResult<Venture> search(VentureSearchCriteria criteria) {
        StringBuilder countJpql = new StringBuilder(
                "SELECT COUNT(DISTINCT v) FROM VentureJpa v WHERE v.status = :status AND v.active = TRUE ");
        StringBuilder dataJpql = new StringBuilder(
                "SELECT v FROM VentureJpa v WHERE v.status = :status AND v.active = TRUE ");
        Map<String, Object> params = new HashMap<>();
        params.put("status", Status.PUBLISHED);

        // Búsqueda por palabras normalizadas en cualquier orden
        List<String> tokens = TextNormalizer.tokenize(criteria.query());
        for (int i = 0; i < tokens.size(); i++) {
            String paramName = "token_" + i;
            countJpql.append("AND v.nameSearch LIKE :").append(paramName).append(" ");
            dataJpql.append("AND v.nameSearch LIKE :").append(paramName).append(" ");
            params.put(paramName, "%" + TextNormalizer.escapeLikePattern(tokens.get(i)) + "%");
        }

        // Filtro por categorías
        if (criteria.categories() != null && !criteria.categories().isEmpty()) {
            countJpql.append("AND v.category IN :categories ");
            dataJpql.append("AND v.category IN :categories ");
            params.put("categories", criteria.categories());
        }

        // Filtro por modos de servicio
        if (criteria.modes() != null && !criteria.modes().isEmpty()) {
            countJpql.append("AND EXISTS (SELECT 1 FROM v.serviceModes sm WHERE sm IN :modes) ");
            dataJpql.append("AND EXISTS (SELECT 1 FROM v.serviceModes sm WHERE sm IN :modes) ");
            params.put("modes", criteria.modes());
        }

        // Conteo total
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();
        if (total == null || total == 0) {
            return PageResult.empty(criteria.pageRequest().page(), criteria.pageRequest().size());
        }

        // Consulta de datos según ordenamiento
        List<VentureJpa> entities;
        if (VentureSearchCriteria.SORT_RATING.equalsIgnoreCase(criteria.sort())) {
            StringBuilder idJpql = new StringBuilder(
                    "SELECT v.ventureId FROM VentureJpa v " +
                    "LEFT JOIN VentureRatingJpa r ON r.id.ventureId = v.ventureId " +
                    "WHERE v.status = :status AND v.active = TRUE ");

            for (int i = 0; i < tokens.size(); i++) {
                idJpql.append("AND v.nameSearch LIKE :token_").append(i).append(" ");
            }
            if (criteria.categories() != null && !criteria.categories().isEmpty()) {
                idJpql.append("AND v.category IN :categories ");
            }
            if (criteria.modes() != null && !criteria.modes().isEmpty()) {
                idJpql.append("AND EXISTS (SELECT 1 FROM v.serviceModes sm WHERE sm IN :modes) ");
            }
            idJpql.append("GROUP BY v.ventureId, v.name ");
            idJpql.append("ORDER BY COALESCE(AVG(r.score), 0.0) DESC, COUNT(r.score) DESC, LOWER(v.name) ASC");

            TypedQuery<String> idQuery = entityManager.createQuery(idJpql.toString(), String.class);
            params.forEach(idQuery::setParameter);
            idQuery.setFirstResult((int) criteria.pageRequest().offset());
            idQuery.setMaxResults(criteria.pageRequest().size());
            List<String> pageIds = idQuery.getResultList();

            if (pageIds.isEmpty()) {
                return PageResult.empty(criteria.pageRequest().page(), criteria.pageRequest().size());
            }

            // Preservar el orden exacto de los IDs obtenidos
            List<VentureJpa> unordered = entityManager.createQuery(
                    "SELECT v FROM VentureJpa v WHERE v.ventureId IN :ids", VentureJpa.class)
                    .setParameter("ids", pageIds)
                    .getResultList();

            Map<String, VentureJpa> byId = new HashMap<>();
            for (VentureJpa v : unordered) {
                byId.put(v.getVentureId(), v);
            }
            entities = new ArrayList<>();
            for (String id : pageIds) {
                VentureJpa v = byId.get(id);
                if (v != null) {
                    entities.add(v);
                }
            }
        } else {
            dataJpql.append("ORDER BY LOWER(v.name) ASC");
            TypedQuery<VentureJpa> dataQuery = entityManager.createQuery(dataJpql.toString() , VentureJpa.class);
            params.forEach(dataQuery::setParameter);
            dataQuery.setFirstResult((int) criteria.pageRequest().offset());
            dataQuery.setMaxResults(criteria.pageRequest().size());
            entities = dataQuery.getResultList();
        }

        List<Venture> ventures = entities.stream().map(this::toDomain).toList();
        return PageResult.of(ventures, total, criteria.pageRequest());
    }

    @Override
    public List<Venture> findByOwnerId(AccountId ownerId) {
        if (ownerId == null) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                "SELECT v FROM VentureJpa v WHERE v.ownerId = :ownerId ORDER BY v.createdAt DESC", VentureJpa.class)
                .setParameter("ownerId", ownerId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<Venture> findAdmin(Status status, PageRequest pageRequest) {
        PageRequest pr = pageRequest != null ? pageRequest : PageRequest.first();
        String countJpql = status != null
                ? "SELECT COUNT(v) FROM VentureJpa v WHERE v.status = :status"
                : "SELECT COUNT(v) FROM VentureJpa v";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        if (status != null) {
            countQuery.setParameter("status", status);
        }
        Long total = countQuery.getSingleResult();
        if (total == null || total == 0) {
            return PageResult.empty(pr.page(), pr.size());
        }

        String dataJpql = status != null
                ? "SELECT v FROM VentureJpa v WHERE v.status = :status ORDER BY v.createdAt DESC"
                : "SELECT v FROM VentureJpa v ORDER BY v.createdAt DESC";

        TypedQuery<VentureJpa> dataQuery = entityManager.createQuery(dataJpql, VentureJpa.class);
        if (status != null) {
            dataQuery.setParameter("status", status);
        }
        dataQuery.setFirstResult((int) pr.offset());
        dataQuery.setMaxResults(pr.size());

        List<Venture> ventures = dataQuery.getResultList().stream().map(this::toDomain).toList();
        return PageResult.of(ventures, total, pr);
    }

    @Override
    @Transactional
    public void delete(VentureId id) {
        if (id == null) {
            return;
        }
        VentureJpa entity = entityManager.find(VentureJpa.class, id.value());
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    // ── Mapeos JPA ↔ Dominio ──────────────────────────────────────────

    private VentureJpa toEntity(Venture v) {
        VentureJpa entity = new VentureJpa();
        entity.setVentureId(v.getId().value());
        entity.setName(v.getName());
        entity.setDescription(v.getDescription());
        entity.setCategory(v.getCategory());
        entity.setOwnerId(v.getOwnerId().value());
        entity.setContactType(v.getContact().type());
        entity.setContactValue(v.getContact().value());
        entity.setServiceModes(new HashSet<>(v.getServiceModes()));
        entity.setLocation(v.getLocation());
        entity.setActive(v.isActive());
        entity.setStatus(v.getStatus());
        entity.setCreatedAt(v.getCreatedAt());
        entity.setUpdatedAt(v.getUpdatedAt());
        entity.setPublishedAt(v.getPublishedAt());
        entity.setNameSearch(v.getNameSearch());
        return entity;
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

