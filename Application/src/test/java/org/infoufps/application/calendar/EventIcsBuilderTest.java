package org.infoufps.application.calendar;

import org.infoufps.application.calendar.CalendarFileDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EventIcsBuilder: generación de archivos iCalendar")
class EventIcsBuilderTest {

    private static final EventId EVENT_ID = EventId.of("11111111-1111-1111-1111-111111111111");

    private static Event event(EventSchedule schedule, int revision, String title, String location) {
        LocalDateTime now = BogotaTime.now();
        return Event.of(EVENT_ID, title, EventType.ACADEMICO, location, schedule,
                "Descripción, con; caracteres\\especiales", "https://ufps.edu.co/evento",
                Status.PUBLISHED, AccountId.of("account-1"), true, now, now, now, revision);
    }

    @Test
    @DisplayName("Un evento con horas usa UTC y un UID estable")
    void timedEventUsesUtc() {
        EventSchedule schedule = EventSchedule.of(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10),
                LocalTime.of(8, 0), LocalTime.of(10, 30));
        Event event = event(schedule, 3, "Feria de ciencia", "Bloque A");

        CalendarFileDto file = EventIcsBuilder.build(event);

        assertEquals("evento-" + EVENT_ID.value() + ".ics", file.fileName());
        assertTrue(file.content().contains("BEGIN:VCALENDAR\r\n"));
        assertTrue(file.content().contains("UID:event-" + EVENT_ID.value() + "@infoufps\r\n"));
        assertTrue(file.content().contains("SEQUENCE:3\r\n"));
        assertTrue(file.content().contains("DTSTART:20260310T130000Z\r\n"));
        assertTrue(file.content().contains("DTEND:20260310T153000Z\r\n"));
        assertTrue(file.content().contains("END:VCALENDAR\r\n"));
    }

    @Test
    @DisplayName("Un evento de todo el día usa DTEND exclusivo")
    void allDayEventUsesExclusiveEnd() {
        EventSchedule schedule = EventSchedule.of(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11), null, null);
        Event event = event(schedule, 0, "Jornada institucional", "Campus");

        String content = EventIcsBuilder.build(event).content();

        assertTrue(content.contains("DTSTART;VALUE=DATE:20260310\r\n"));
        assertTrue(content.contains("DTEND;VALUE=DATE:20260312\r\n"));
        assertFalse(content.contains("DTSTART:"));
    }

    @Test
    @DisplayName("El contenido usa CRLF en todas las líneas")
    void contentUsesCrlf() {
        EventSchedule schedule = EventSchedule.of(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10), null, null);

        String content = EventIcsBuilder.build(event(schedule, 0, "Evento", "Lugar")).content();

        assertFalse(content.replace("\r\n", "").contains("\n"));
        assertFalse(content.replace("\r\n", "").contains("\r"));
    }

    @Test
    @DisplayName("Se escapan comas, punto y coma, barras invertidas y saltos de línea")
    void escapesText() {
        assertEquals("a\\, b\\; c\\\\ d\\ne", EventIcsBuilder.escapeText("a, b; c\\ d\ne"));
        assertEquals("a\\nb", EventIcsBuilder.escapeText("a\r\nb"));
        assertEquals("", EventIcsBuilder.escapeText(null));
    }

    @Test
    @DisplayName("Las líneas largas se pliegan a 75 octetos con continuación")
    void foldsLongLines() {
        String original = "x".repeat(200);

        String folded = EventIcsBuilder.fold(original);
        String[] lines = folded.split("\r\n", -1);

        assertTrue(lines.length > 1);
        assertEquals(75, lines[0].getBytes(StandardCharsets.UTF_8).length);
        for (int i = 1; i < lines.length; i++) {
            assertTrue(lines[i].startsWith(" "));
            assertTrue(lines[i].getBytes(StandardCharsets.UTF_8).length <= 75);
        }

        StringBuilder reconstructed = new StringBuilder(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            reconstructed.append(lines[i].substring(1));
        }
        assertEquals(original, reconstructed.toString());
    }

    @Test
    @DisplayName("El plegado no parte caracteres UTF-8 multibyte")
    void foldingKeepsMultibyteCharactersIntact() {
        String original = "á".repeat(100);

        String folded = EventIcsBuilder.fold(original);
        String[] lines = folded.split("\r\n", -1);

        StringBuilder reconstructed = new StringBuilder(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            reconstructed.append(lines[i].substring(1));
        }

        assertEquals(original, reconstructed.toString());
        for (String line : lines) {
            assertFalse(line.contains("\uFFFD"));
            assertTrue(line.getBytes(StandardCharsets.UTF_8).length <= 75);
        }
    }

    @Test
    @DisplayName("El título y el lugar se escapan dentro del archivo")
    void titleAndLocationAreEscaped() {
        EventSchedule schedule = EventSchedule.of(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10), null, null);
        Event event = event(schedule, 0, "Charla, cultura; arte", "Sala 1\\2");

        String content = EventIcsBuilder.build(event).content();

        assertTrue(content.contains("SUMMARY:Charla\\, cultura\\; arte\r\n"));
        assertTrue(content.contains("LOCATION:Sala 1\\\\2\r\n"));
    }
}
