package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EventSchedule: reglas de fechas y horas")
class EventScheduleTest {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 10);

    @Test
    @DisplayName("La fecha de fin por defecto es la fecha de inicio")
    void endDateDefaultsToStartDate() {
        EventSchedule schedule = EventSchedule.of(DAY, null, null, null);

        assertEquals(DAY, schedule.startDate());
        assertEquals(DAY, schedule.endDate());
        assertTrue(schedule.isAllDay());
        assertFalse(schedule.isMultiDay());
        assertNull(schedule.startTime());
        assertNull(schedule.endTime());
    }

    @Test
    @DisplayName("La fecha de inicio es obligatoria")
    void startDateIsRequired() {
        assertThrows(DomainException.class, () -> EventSchedule.of(null, null, null, null));
    }

    @Test
    @DisplayName("La fecha de fin no puede ser anterior a la de inicio")
    void endDateCannotBeBeforeStartDate() {
        assertThrows(DomainException.class,
                () -> EventSchedule.of(DAY, DAY.minusDays(1), null, null));
    }

    @Test
    @DisplayName("Las horas deben informarse juntas")
    void timesMustBeProvidedTogether() {
        assertThrows(DomainException.class,
                () -> EventSchedule.of(DAY, DAY, LocalTime.of(8, 0), null));
        assertThrows(DomainException.class,
                () -> EventSchedule.of(DAY, DAY, null, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Las horas solo aplican a eventos de un único día")
    void timesOnlyApplyToSingleDayEvents() {
        assertThrows(DomainException.class, () -> EventSchedule.of(
                DAY, DAY.plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("La hora de fin debe ser estrictamente posterior a la de inicio")
    void endTimeMustBeAfterStartTime() {
        assertThrows(DomainException.class, () -> EventSchedule.of(
                DAY, DAY, LocalTime.of(10, 0), LocalTime.of(10, 0)));
        assertThrows(DomainException.class, () -> EventSchedule.of(
                DAY, DAY, LocalTime.of(10, 0), LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("Un evento con horas no es de todo el día")
    void timedEventIsNotAllDay() {
        EventSchedule schedule = EventSchedule.of(
                DAY, DAY, LocalTime.of(8, 0), LocalTime.of(10, 30));

        assertFalse(schedule.isAllDay());
        assertFalse(schedule.isMultiDay());
        assertEquals(LocalTime.of(8, 0), schedule.startTime());
        assertEquals(LocalTime.of(10, 30), schedule.endTime());
    }

    @Test
    @DisplayName("Un evento con fechas distintas es de varios días")
    void multiDayEvent() {
        EventSchedule schedule = EventSchedule.of(DAY, DAY.plusDays(3), null, null);

        assertTrue(schedule.isAllDay());
        assertTrue(schedule.isMultiDay());
    }

    @Test
    @DisplayName("La conversión a UTC aplica el desfase fijo de America/Bogota (UTC-5)")
    void convertsToUtc() {
        EventSchedule schedule = EventSchedule.of(
                DAY, DAY, LocalTime.of(8, 0), LocalTime.of(10, 30));

        assertEquals(DAY.atTime(13, 0).toInstant(ZoneOffset.UTC), schedule.startUtc().toInstant());
        assertEquals(DAY.atTime(15, 30).toInstant(ZoneOffset.UTC), schedule.endUtc().toInstant());
        assertEquals(ZoneOffset.UTC, schedule.startUtc().getOffset());
    }

    @Test
    @DisplayName("Un evento de todo el día termina de forma exclusiva al día siguiente")
    void allDayEndIsExclusive() {
        EventSchedule schedule = EventSchedule.of(DAY, DAY.plusDays(2), null, null);

        assertEquals(DAY.atStartOfDay().plusHours(5).toInstant(ZoneOffset.UTC),
                schedule.startUtc().toInstant());
        assertEquals(DAY.plusDays(3).atStartOfDay().plusHours(5).toInstant(ZoneOffset.UTC),
                schedule.endUtc().toInstant());
    }

    @Test
    @DisplayName("Dos programaciones con los mismos valores son iguales")
    void equality() {
        EventSchedule first = EventSchedule.of(DAY, DAY, LocalTime.of(8, 0), LocalTime.of(9, 0));
        EventSchedule second = EventSchedule.create(DAY, DAY, LocalTime.of(8, 0), LocalTime.of(9, 0));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
