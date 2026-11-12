package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.application.calendar.FixedCalendarSettings;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EventShareSupport: textos y enlaces para compartir")
class EventShareSupportTest {

    private static final FixedCalendarSettings SETTINGS = FixedCalendarSettings.defaults();

    private static Event event(EventSchedule schedule) {
        LocalDateTime now = BogotaTime.now();
        return Event.of(EventId.of("event-1"), "Feria de ciencia", EventType.ACADEMICO,
                "Bloque A", schedule, null, null, Status.PUBLISHED,
                AccountId.of("account-1"), true, now, now, now, 0);
    }

    @Test
    @DisplayName("Une base y ruta sin duplicar barras")
    void joinsUrl() {
        assertEquals("https://info.ufps.edu.co/eventos/1",
                EventShareSupport.joinUrl("https://info.ufps.edu.co/", "/eventos/1"));
        assertEquals("https://info.ufps.edu.co/eventos/1",
                EventShareSupport.joinUrl("https://info.ufps.edu.co", "eventos/1"));
    }

    @Test
    @DisplayName("Construye la URL del frontend reemplazando {id}")
    void buildsEventUrl() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), null, null));

        assertEquals("https://info.ufps.edu.co/eventos/event-1",
                EventShareSupport.eventUrl(event, SETTINGS));
    }

    @Test
    @DisplayName("Construye la URL de descarga del .ics")
    void buildsIcsUrl() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), null, null));

        assertEquals("https://api.ufps.edu.co/api/v1/public/events/event-1/calendar.ics",
                EventShareSupport.icsUrl(event, SETTINGS));
    }

    @Test
    @DisplayName("Describe un evento con horas")
    void describesTimedEvent() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), LocalTime.of(8, 0), LocalTime.of(10, 30)));

        assertEquals("10/03/2026 de 08:00 a 10:30", EventShareSupport.scheduleText(event));
        assertEquals("10/03/2026 de 08:00 a 10:30 · Bloque A", EventShareSupport.description(event));
    }

    @Test
    @DisplayName("Describe un evento de un día completo")
    void describesSingleAllDayEvent() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), null, null));

        assertEquals("10/03/2026 (todo el día)", EventShareSupport.scheduleText(event));
    }

    @Test
    @DisplayName("Describe un evento de varios días")
    void describesMultiDayEvent() {
        Event event = event(EventSchedule.of(LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12), null, null));

        assertEquals("Del 10/03/2026 al 12/03/2026", EventShareSupport.scheduleText(event));
    }
}
