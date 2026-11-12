package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IVentureRatingRepository;
import org.infoufps.domain.venture.RatingSummary;
import org.infoufps.domain.venture.VentureId;
import org.infoufps.domain.venture.VentureRating;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryVentureRatingRepository implements IVentureRatingRepository {

    private final Map<String, VentureRating> storage = new ConcurrentHashMap<>();

    private String key(AccountId accountId, VentureId ventureId) {
        return accountId.value() + ":" + ventureId.value();
    }

    @Override
    public VentureRating save(VentureRating rating) {
        storage.put(key(rating.getAccountId(), rating.getVentureId()), rating);
        return rating;
    }

    @Override
    public Optional<VentureRating> findByAccountIdAndVentureId(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(key(accountId, ventureId)));
    }

    @Override
    public void delete(AccountId accountId, VentureId ventureId) {
        if (accountId != null && ventureId != null) {
            storage.remove(key(accountId, ventureId));
        }
    }

    @Override
    public RatingSummary getRatingSummary(VentureId ventureId) {
        if (ventureId == null) return RatingSummary.empty();
        List<VentureRating> ratings = storage.values().stream()
                .filter(r -> r.getVentureId().equals(ventureId))
                .toList();

        if (ratings.isEmpty()) {
            return RatingSummary.empty();
        }
        double avg = ratings.stream().mapToInt(VentureRating::getScore).average().orElse(0.0);
        return RatingSummary.of(avg, (long) ratings.size());
    }

    @Override
    public Map<VentureId, RatingSummary> getRatingSummaries(List<VentureId> ventureIds) {
        Map<VentureId, RatingSummary> map = new HashMap<>();
        if (ventureIds == null) return map;
        for (VentureId id : ventureIds) {
            map.put(id, getRatingSummary(id));
        }
        return map;
    }
}

