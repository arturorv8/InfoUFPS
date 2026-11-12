package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EventQueryValidator: rangos y filtros")
class EventQueryValidatorTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);

    @Test
    @DisplayName("Un rango de 62 días es válido")
    void sixtyTwoDayRangeIsValid() {
        LocalDate to = FROM.plusDays(61);

        EventRangeCriteria criteria = EventQueryValidator.validateRange(FROM, to, null);

        assertEquals(FROM, criteria.from());
        assertEquals(to, criteria.to());
        assertTrue(criteria.types().isEmpty());
    }

    @Test
    @DisplayName("Un rango de 63 días es rechazado")
    void sixtyThreeDayRangeIsRejected() {
        assertThrows(DomainException.class,
                () -> EventQueryValidator.validateRange(FROM, FROM.plusDays(62), null));
    }

    @Test
    @DisplayName("Las fechas from y to son obligatorias y ordenadas")
    void datesAreRequiredAndOrdered() {
        assertThrows(DomainException.class,
                () -> EventQueryValidator.validateRange(null, FROM, null));
        assertThrows(DomainException.class,
                () -> EventQueryValidator.validateRange(FROM, null, null));
        assertThrows(DomainException.class,
                () -> EventQueryValidator.validateRange(FROM, FROM.minusDays(1), null));
    }

    @Test
    @DisplayName("Los tipos aceptan varios valores y listas separadas por coma")
    void parsesMultipleTypes() {
        Set<EventType> types = EventQueryValidator.parseTypes(
                List.of("cultural", "ACADEMICO,deportivo", "Cultural", " "));

        assertEquals(Set.of(EventType.CULTURAL, EventType.ACADEMICO, EventType.DEPORTIVO), types);
    }

    @Test
    @DisplayName("Una lista de tipos vacía significa todos")
    void emptyTypesMeansAll() {
        assertTrue(EventQueryValidator.parseTypes(null).isEmpty());
        assertTrue(EventQueryValidator.parseTypes(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Un tipo inválido es rechazado")
    void invalidTypeIsRejected() {
        assertThrows(DomainException.class,
                () -> EventQueryValidator.parseTypes(List.of("NO_EXISTE")));
        assertThrows(DomainException.class,
                () -> EventQueryValidator.parseTypes(List.of("CULTURAL,OTRO")));
    }

    @Test
    @DisplayName("El estado por defecto es UNDER_REVIEW")
    void statusDefaultsToUnderReview() {
        assertEquals(Status.UNDER_REVIEW, EventQueryValidator.parseStatus(null));
        assertEquals(Status.UNDER_REVIEW, EventQueryValidator.parseStatus(" "));
        assertEquals(Status.PUBLISHED, EventQueryValidator.parseStatus(" published "));
        assertEquals(Status.ARCHIVED, EventQueryValidator.parseStatus("ARCHIVED"));
    }

    @Test
    @DisplayName("Un estado inválido es rechazado")
    void invalidStatusIsRejected() {
        assertThrows(DomainException.class, () -> EventQueryValidator.parseStatus("BORRADOR"));
    }
}
