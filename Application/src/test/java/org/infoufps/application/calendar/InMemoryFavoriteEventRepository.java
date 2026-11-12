package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.FavoriteEvent;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del repositorio de favoritos para pruebas unitarias.
 */
public class InMemoryFavoriteEventRepository implements IFavoriteEventRepository {

    private final Map<String, FavoriteEvent> favorites = new LinkedHashMap<>();
    private final Map<String, Event> events;

    public InMemoryFavoriteEventRepository(Map<String, Event> events) {
        this.events = events;
    }

    @Override
    public FavoriteEvent save(FavoriteEvent favorite) {
        favorites.put(key(favorite.getAccountId(), favorite.getEventId()), favorite);
        return favorite;
    }

    @Override
    public Optional<FavoriteEvent> findById(AccountId accountId, EventId eventId) {
        return Optional.ofNullable(favorites.get(key(accountId, eventId)));
    }

    @Override
    public boolean exists(AccountId accountId, EventId eventId) {
        return favorites.containsKey(key(accountId, eventId));
    }

    @Override
    public void delete(AccountId accountId, EventId eventId) {
        favorites.remove(key(accountId, eventId));
    }

    @Override
    public List<EventId> findPublishedEventIdsByAccount(AccountId accountId) {
        List<EventId> result = new ArrayList<>();
        for (FavoriteEvent favorite : favorites.values()) {
            if (!favorite.getAccountId().equals(accountId)) {
                continue;
            }
            Event event = events.get(favorite.getEventId().value());
            if (event != null && event.getStatus() == Status.PUBLISHED) {
                result.add(favorite.getEventId());
            }
        }
        return result;
    }

    @Override
    public PageResult<Event> findPublishedFavoriteEvents(AccountId accountId, PageRequest pageRequest) {
        List<Event> result = new ArrayList<>();
        for (FavoriteEvent favorite : favorites.values()) {
            if (!favorite.getAccountId().equals(accountId)) {
                continue;
            }
            Event event = events.get(favorite.getEventId().value());
            if (event != null && event.getStatus() == Status.PUBLISHED) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(Event::getStartDate));
        PageRequest normalized = pageRequest != null ? pageRequest : PageRequest.first();
        int from = (int) Math.min(normalized.offset(), result.size());
        int to = Math.min(from + normalized.size(), result.size());
        return PageResult.of(new ArrayList<>(result.subList(from, to)), result.size(), normalized);
    }

    @Override
    public long countPublishedByAccount(AccountId accountId) {
        return findPublishedEventIdsByAccount(accountId).size();
    }

    /**
     * @return cantidad de favoritos almacenados.
     */
    public int size() {
        return favorites.size();
    }

    private static String key(AccountId accountId, EventId eventId) {
        return accountId.value() + "|" + eventId.value();
    }
}
