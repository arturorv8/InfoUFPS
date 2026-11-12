package org.infoufps.application.calendar;

import org.infoufps.application.calendar.EventDaySummaryDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GetEventSummaryQuery: resumen diario")
class GetEventSummaryQueryTest {

    private static final LocalDate FROM = LocalDate.of(2026, 3, 9);
    private static final LocalDate TO = LocalDate.of(2026, 3, 13);

    private InMemoryEventRepository repository;
    private GetEventSummaryQuery query;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        query = new GetEventSummaryQuery(repository);
    }

    private Event savePublished(String title, EventType type, LocalDate start, LocalDate end) {
        Event event = Event.create(title, type, "Campus",
                EventSchedule.of(start, end, null, null), null, null,
                AccountId.of("account-1"), true);
        event.publish();
        return repository.save(event);
    }

    @Test
    @DisplayName("Devuelve todos los días del rango, incluidos los que no tienen eventos")
    void returnsEveryDayInRange() {
        savePublished("Jornada", EventType.ACADEMICO, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
        savePublished("Charla", EventType.CULTURAL, LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 11));

        List<EventDaySummaryDto> summary = query.execute(FROM, TO, null);
        Map<LocalDate, Long> counts = summary.stream()
                .collect(Collectors.toMap(EventDaySummaryDto::date, EventDaySummaryDto::count));

        assertEquals(5, summary.size());
        assertEquals(0, counts.get(LocalDate.of(2026, 3, 9)));
        assertEquals(1, counts.get(LocalDate.of(2026, 3, 10)));
        assertEquals(2, counts.get(LocalDate.of(2026, 3, 11)));
        assertEquals(1, counts.get(LocalDate.of(2026, 3, 12)));
        assertEquals(0, counts.get(LocalDate.of(2026, 3, 13)));
    }

    @Test
    @DisplayName("Cuenta un evento de varios días en cada día que abarca")
    void countsMultiDayEventOnEveryDay() {
        savePublished("Semana cultural", EventType.CULTURAL, FROM, TO);

        Map<LocalDate, Long> counts = query.execute(FROM, TO, null).stream()
                .collect(Collectors.toMap(EventDaySummaryDto::date, EventDaySummaryDto::count));

        assertEquals(5, counts.size());
        counts.values().forEach(count -> assertEquals(1, count));
    }

    @Test
    @DisplayName("Filtra por tipo de evento")
    void filtersByType() {
        savePublished("Jornada", EventType.ACADEMICO, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10));
        savePublished("Concierto", EventType.CULTURAL, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10));

        List<EventDaySummaryDto> summary = query.execute(FROM, TO, List.of("cultural"));

        assertEquals(1, summary.stream()
                .filter(day -> day.date().equals(LocalDate.of(2026, 3, 10)))
                .map(EventDaySummaryDto::count)
                .findFirst()
                .orElseThrow());
    }

    @Test
    @DisplayName("Rechaza un rango mayor a 62 días")
    void rejectsTooWideRange() {
        assertThrows(DomainException.class,
                () -> query.execute(FROM, FROM.plusDays(62), null));
    }
}
