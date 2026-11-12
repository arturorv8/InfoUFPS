package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.EventDuplicateCriteria;
import org.infoufps.domain.calendar.EventPageCriteria;
import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.domain.calendar.ModerationCriteria;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementación en memoria del repositorio de eventos para pruebas unitarias.
 * Reproduce el comportamiento de filtrado definido en los criterios del dominio.
 */
public class InMemoryEventRepository implements IEventRepository {

    private final Map<String, Event> events = new LinkedHashMap<>();

    @Override
    public Event save(Event event) {
        events.put(event.getId().value(), event);
        return event;
    }

    @Override
    public Optional<Event> findById(EventId id) {
        return Optional.ofNullable(events.get(id.value()));
    }

    @Override
    public List<Event> findPublishedOverlapping(EventRangeCriteria criteria) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            if (event.getStatus() != Status.PUBLISHED) {
                continue;
            }
            if (criteria.hasTypes() && !criteria.types().contains(event.getType())) {
                continue;
            }
            boolean overlaps = !event.getStartDate().isAfter(criteria.to())
                    && !event.getEndDate().isBefore(criteria.from());
            if (overlaps) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(Event::getStartDate)
                .thenComparing(e -> e.getStartTime() == null ? java.time.LocalTime.MIN : e.getStartTime())
                .thenComparing(Event::getTitle));
        return result;
    }

    @Override
    public PageResult<Event> findPublishedUpcomingOrOngoing(EventPageCriteria criteria) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            if (event.getStatus() != Status.PUBLISHED) {
                continue;
            }
            if (criteria.hasTypes() && !criteria.types().contains(event.getType())) {
                continue;
            }
            if (!event.getEndDate().isBefore(criteria.today())) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(Event::getStartDate)
                .thenComparing(e -> e.getStartTime() == null ? java.time.LocalTime.MIN : e.getStartTime())
                .thenComparing(Event::getTitle));
        return page(result, criteria.pageRequest());
    }

    @Override
    public PageResult<Event> findByProposedBy(AccountId accountId, PageRequest pageRequest) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            if (event.getProposedBy().equals(accountId)) {
                result.add(event);
            }
        }
        return page(result, pageRequest);
    }

    @Override
    public PageResult<Event> findByStatus(ModerationCriteria criteria) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            if (event.getStatus() == criteria.status()) {
                result.add(event);
            }
        }
        return page(result, criteria.pageRequest());
    }

    @Override
    public List<Event> findDuplicates(EventDuplicateCriteria criteria) {
        List<Event> result = new ArrayList<>();
        for (Event event : events.values()) {
            if (event.getStatus() == Status.ARCHIVED) {
                continue;
            }
            if (criteria.excludeId() != null && event.getId().equals(criteria.excludeId())) {
                continue;
            }
            boolean sameTitle = event.getTitle().equalsIgnoreCase(criteria.title());
            boolean sameDate = event.getStartDate().equals(criteria.startDate());
            boolean sameTime = Objects.equals(event.getStartTime(), criteria.startTime());
            if (sameTitle && sameDate && sameTime) {
                result.add(event);
            }
        }
        return result;
    }

    @Override
    public long countProposalsByAccountOnDate(AccountId accountId, LocalDate date) {
        return events.values().stream()
                .filter(event -> event.getProposedBy().equals(accountId))
                .filter(event -> event.getCreatedAt().toLocalDate().equals(date))
                .count();
    }

    @Override
    public void deleteById(EventId id) {
        events.remove(id.value());
    }

    @Override
    public boolean existsById(EventId id) {
        return events.containsKey(id.value());
    }

    /**
     * @return cantidad de eventos almacenados.
     */
    public int size() {
        return events.size();
    }

    /**
     * @return vista de los eventos almacenados indexados por id.
     */
    public Map<String, Event> snapshot() {
        return events;
    }

    private static PageResult<Event> page(List<Event> all, PageRequest request) {
        PageRequest normalized = request != null ? request : PageRequest.first();
        int from = (int) Math.min(normalized.offset(), all.size());
        int to = Math.min(from + normalized.size(), all.size());
        return PageResult.of(new ArrayList<>(all.subList(from, to)), all.size(), normalized);
    }
}
