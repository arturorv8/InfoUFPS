package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.venture.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryVentureRepository implements IVentureRepository {

    private final Map<String, Venture> storage = new ConcurrentHashMap<>();
    private Function<VentureId, RatingSummary> ratingProvider = id -> RatingSummary.empty();

    public void setRatingProvider(Function<VentureId, RatingSummary> provider) {
        this.ratingProvider = provider;
    }

    @Override
    public Venture save(Venture venture) {
        storage.put(venture.getId().value(), venture);
        return venture;
    }

    @Override
    public Optional<Venture> findById(VentureId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id.value()));
    }

    @Override
    public boolean existsByOwnerIdAndNameSearch(AccountId ownerId, String nameSearch, VentureId excludeId) {
        return storage.values().stream()
                .filter(v -> v.getOwnerId().equals(ownerId))
                .filter(v -> v.getNameSearch().equals(nameSearch))
                .anyMatch(v -> excludeId == null || !v.getId().equals(excludeId));
    }

    @Override
    public boolean existsByNameSearchAndDifferentOwner(String nameSearch, AccountId ownerId) {
        return storage.values().stream()
                .filter(v -> ownerId == null || !v.getOwnerId().equals(ownerId))
                .anyMatch(v -> v.getNameSearch().equals(nameSearch));
    }

    @Override
    public boolean existsByContactValueAndDifferentOwner(String contactValue, AccountId ownerId) {
        return storage.values().stream()
                .filter(v -> ownerId == null || !v.getOwnerId().equals(ownerId))
                .anyMatch(v -> v.getContact().value().equalsIgnoreCase(contactValue));
    }

    @Override
    public long countByOwnerId(AccountId ownerId) {
        return storage.values().stream()
                .filter(v -> v.getOwnerId().equals(ownerId))
                .count();
    }

    @Override
    public long countCreatedByOwnerIdSince(AccountId ownerId, LocalDateTime since) {
        return storage.values().stream()
                .filter(v -> v.getOwnerId().equals(ownerId))
                .filter(v -> !v.getCreatedAt().isBefore(since))
                .count();
    }

    @Override
    public PageResult<Venture> search(VentureSearchCriteria criteria) {
        List<Venture> filtered = storage.values().stream()
                .filter(Venture::isVisible)
                .filter(v -> {
                    if (criteria.query() == null || criteria.query().isBlank()) return true;
                    List<String> tokens = TextNormalizer.tokenize(criteria.query());
                    for (String token : tokens) {
                        if (!v.getNameSearch().contains(token)) return false;
                    }
                    return true;
                })
                .filter(v -> {
                    if (criteria.categories() == null || criteria.categories().isEmpty()) return true;
                    return criteria.categories().contains(v.getCategory());
                })
                .filter(v -> {
                    if (criteria.modes() == null || criteria.modes().isEmpty()) return true;
                    return criteria.modes().stream().anyMatch(m -> v.getServiceModes().contains(m));
                })
                .sorted((v1, v2) -> {
                    if (VentureSearchCriteria.SORT_RATING.equalsIgnoreCase(criteria.sort())) {
                        RatingSummary s1 = ratingProvider.apply(v1.getId());
                        RatingSummary s2 = ratingProvider.apply(v2.getId());
                        int cmpAvg = Double.compare(s2.average(), s1.average());
                        if (cmpAvg != 0) return cmpAvg;
                        int cmpTot = Long.compare(s2.total(), s1.total());
                        if (cmpTot != 0) return cmpTot;
                    }
                    return v1.getName().compareToIgnoreCase(v2.getName());
                })
                .toList();

        int fromIndex = (int) Math.min(criteria.pageRequest().offset(), filtered.size());
        int toIndex = Math.min(fromIndex + criteria.pageRequest().size(), filtered.size());
        List<Venture> page = filtered.subList(fromIndex, toIndex);

        return PageResult.of(page, filtered.size(), criteria.pageRequest());
    }

    @Override
    public List<Venture> findByOwnerId(AccountId ownerId) {
        return storage.values().stream()
                .filter(v -> v.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Venture::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public PageResult<Venture> findAdmin(Status status, PageRequest pageRequest) {
        List<Venture> list = storage.values().stream()
                .filter(v -> status == null || v.getStatus() == status)
                .sorted(Comparator.comparing(Venture::getCreatedAt).reversed())
                .toList();

        int fromIndex = (int) Math.min(pageRequest.offset(), list.size());
        int toIndex = Math.min(fromIndex + pageRequest.size(), list.size());
        List<Venture> page = list.subList(fromIndex, toIndex);
        return PageResult.of(page, list.size(), pageRequest);
    }

    @Override
    public void delete(VentureId id) {
        if (id != null) {
            storage.remove(id.value());
        }
    }
}

