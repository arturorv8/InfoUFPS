package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryFavoriteVentureRepository implements IFavoriteVentureRepository {

    private final Map<String, FavoriteVenture> storage = new ConcurrentHashMap<>();
    private final Function<VentureId, Optional<Venture>> ventureProvider;

    public InMemoryFavoriteVentureRepository(Function<VentureId, Optional<Venture>> ventureProvider) {
        this.ventureProvider = ventureProvider;
    }

    private String key(AccountId accountId, VentureId ventureId) {
        return accountId.value() + ":" + ventureId.value();
    }

    @Override
    public FavoriteVenture save(FavoriteVenture favorite) {
        storage.putIfAbsent(key(favorite.getAccountId(), favorite.getVentureId()), favorite);
        return favorite;
    }

    @Override
    public boolean existsByAccountIdAndVentureId(AccountId accountId, VentureId ventureId) {
        if (accountId == null || ventureId == null) return false;
        return storage.containsKey(key(accountId, ventureId));
    }

    @Override
    public void delete(AccountId accountId, VentureId ventureId) {
        if (accountId != null && ventureId != null) {
            storage.remove(key(accountId, ventureId));
        }
    }

    @Override
    public List<VentureId> findFavoriteVentureIdsByAccountId(AccountId accountId) {
        if (accountId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(f -> f.getAccountId().equals(accountId))
                .map(FavoriteVenture::getVentureId)
                .toList();
    }

    @Override
    public PageResult<Venture> findFavoriteVenturesByAccountId(AccountId accountId, PageRequest pageRequest) {
        if (accountId == null) return PageResult.empty(pageRequest.page(), pageRequest.size());

        List<Venture> published = storage.values().stream()
                .filter(f -> f.getAccountId().equals(accountId))
                .sorted(Comparator.comparing(FavoriteVenture::getCreatedAt).reversed())
                .map(f -> ventureProvider.apply(f.getVentureId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus() == Status.PUBLISHED)
                .toList();

        int fromIndex = (int) Math.min(pageRequest.offset(), published.size());
        int toIndex = Math.min(fromIndex + pageRequest.size(), published.size());
        List<Venture> page = published.subList(fromIndex, toIndex);

        return PageResult.of(page, published.size(), pageRequest);
    }
}

