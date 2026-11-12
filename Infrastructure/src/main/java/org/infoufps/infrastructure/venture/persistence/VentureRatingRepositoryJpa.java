package org.infoufps.infrastructure.venture.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IVentureRatingRepository;
import org.infoufps.domain.venture.RatingSummary;
import org.infoufps.domain.venture.VentureId;
import org.infoufps.domain.venture.VentureRating;

import java.util.*;

/**
 * Adaptador de Persistencia JPA para {@link IVentureRatingRepository}.
 */
@ApplicationScoped
public class VentureRatingRepositoryJpa implements IVentureRatingRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public VentureRating save(VentureRating rating) {
        VentureRatingIdJpa id = new VentureRatingIdJpa(
                rating.getAccountId().value(),
                rating.getVentureId().value()
        );
        VentureRatingJpa existing = entityManager.find(VentureRatingJpa.class, id);
        if (existing != null) {
            existing.setScore(rating.getScore());
            existing.setUpdatedAt(rating.getUpdatedAt());
            return rating;
        }

        VentureRatingJpa entity = new VentureRatingJpa(
                id,
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
        entityManager.persist(entity);
        return rating;
    }

    @Override
    public Optional<VentureRating> findByAccountIdAndVentureId(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) {
            return Optional.empty();
        }
        VentureRatingIdJpa id = new VentureRatingIdJpa(accountId.value(), ventureId.value());
        VentureRatingJpa entity = entityManager.find(VentureRatingJpa.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    @Transactional
    public void delete(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) {
            return;
        }
        VentureRatingIdJpa id = new VentureRatingIdJpa(accountId.value(), ventureId.value());
        VentureRatingJpa entity = entityManager.find(VentureRatingJpa.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public RatingSummary getRatingSummary(VentureId ventureId) {
        if (ventureId == null) {
            return RatingSummary.empty();
        }
        List<Object[]> result = entityManager.createQuery(
                "SELECT AVG(r.score), COUNT(r.score) FROM VentureRatingJpa r WHERE r.id.ventureId = :ventureId", Object[].class)
                .setParameter("ventureId", ventureId.value())
                .getResultList();

        if (result.isEmpty() || result.get(0) == null) {
            return RatingSummary.empty();
        }
        Object[] row = result.get(0);
        Double avg = (Double) row[0];
        Long total = (Long) row[1];
        return RatingSummary.of(avg, total);
    }

    @Override
    public Map<VentureId, RatingSummary> getRatingSummaries(List<VentureId> ventureIds) {
        if (ventureIds == null || ventureIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> ids = ventureIds.stream().map(VentureId::value).toList();
        List<Object[]> rows = entityManager.createQuery(
                "SELECT r.id.ventureId, AVG(r.score), COUNT(r.score) FROM VentureRatingJpa r " +
                        "WHERE r.id.ventureId IN :ids GROUP BY r.id.ventureId", Object[].class)
                .setParameter("ids", ids)
                .getResultList();

        Map<VentureId, RatingSummary> summaries = new HashMap<>();
        for (Object[] row : rows) {
            String vId = (String) row[0];
            Double avg = (Double) row[1];
            Long total = (Long) row[2];
            summaries.put(VentureId.of(vId), RatingSummary.of(avg, total));
        }

        // Para los que no tengan calificaciones registradas, asignar RatingSummary.empty()
        for (VentureId vId : ventureIds) {
            summaries.putIfAbsent(vId, RatingSummary.empty());
        }
        return summaries;
    }

    private VentureRating toDomain(VentureRatingJpa e) {
        return VentureRating.of(
                AccountId.of(e.getId().getAccountId()),
                VentureId.of(e.getId().getVentureId()),
                e.getScore(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}

