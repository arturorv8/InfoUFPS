package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GoogleCalendarLinkBuilder: enlaces a Google Calendar")
class GoogleCalendarLinkBuilderTest {

    private static Event event(EventSchedule schedule, String title) {
        LocalDateTime now = BogotaTime.now();
        return Event.of(EventId.of("event-1"), title, EventType.ACADEMICO, "Bloque A",
                schedule, "Detalle del evento", "https://ufps.edu.co/evento",
                Status.PUBLISHED, AccountId.of("account-1"), true, now, now, now, 0);
    }

    @Test
    @DisplayName("Un evento con horas usa fechas UTC")
    void timedEventUsesUtcDates() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), LocalTime.of(8, 0), LocalTime.of(10, 30)),
                "Feria de ciencia");

        String link = GoogleCalendarLinkBuilder.build(event);

        assertTrue(link.startsWith("https://calendar.google.com/calendar/render?action=TEMPLATE"));
        assertTrue(link.contains("dates=20260310T130000Z/20260310T153000Z"));
        assertTrue(link.contains("text=Feria%20de%20ciencia"));
        assertTrue(link.contains("location=Bloque%20A"));
    }

    @Test
    @DisplayName("Un evento de todo el día usa fechas exclusivas")
    void allDayEventUsesExclusiveDates() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 11), null, null), "Jornada");

        String link = GoogleCalendarLinkBuilder.build(event);

        assertTrue(link.contains("dates=20260310/20260312"));
    }

    @Test
    @DisplayName("Codifica caracteres especiales del título")
    void encodesSpecialCharacters() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), null, null), "Cultura & arte");

        String link = GoogleCalendarLinkBuilder.build(event);

        assertTrue(link.contains("text=Cultura%20%26%20arte"));
    }
}
