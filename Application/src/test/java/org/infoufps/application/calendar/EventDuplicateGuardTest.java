package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EventDuplicateGuard: título, fecha y hora")
class EventDuplicateGuardTest {

    private static final AccountId AUTHOR = AccountId.of("account-1");
    private static final LocalDate DAY = BogotaTime.today().plusDays(10);
    private static final LocalTime START = LocalTime.of(8, 0);

    private InMemoryEventRepository repository;
    private EventDuplicateGuard guard;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        guard = new EventDuplicateGuard(repository);
    }

    private Event save(String title, LocalTime startTime, boolean published) {
        LocalTime endTime = startTime != null ? startTime.plusHours(2) : null;
        Event event = Event.create(title, EventType.ACADEMICO, "Bloque A",
                EventSchedule.of(DAY, DAY, startTime, endTime), null, null, AUTHOR, false);
        if (published) {
            event.publish();
        }
        return repository.save(event);
    }

    @Test
    @DisplayName("Rechaza un duplicado publicado sin distinguir mayúsculas")
    void rejectsPublishedDuplicateIgnoringCase() {
        save("Feria de Ciencia", START, true);

        assertThrows(ConflictException.class,
                () -> guard.ensureNoPublishedDuplicate("feria de ciencia", DAY, START, null));
    }

    @Test
    @DisplayName("Permite el duplicado cuando el evento existente está en revisión")
    void allowsDuplicateWhenOnlyUnderReview() {
        save("Feria de Ciencia", START, false);

        assertDoesNotThrow(() -> guard.ensureNoPublishedDuplicate(
                "Feria de Ciencia", DAY, START, null));
    }

    @Test
    @DisplayName("No considera duplicado si cambia la hora de inicio")
    void differentStartTimeIsNotDuplicate() {
        save("Feria de Ciencia", START, true);

        assertDoesNotThrow(() -> guard.ensureNoPublishedDuplicate(
                "Feria de Ciencia", DAY, LocalTime.of(10, 0), null));
    }

    @Test
    @DisplayName("No considera duplicado si cambia la fecha")
    void differentDateIsNotDuplicate() {
        save("Feria de Ciencia", START, true);

        assertDoesNotThrow(() -> guard.ensureNoPublishedDuplicate(
                "Feria de Ciencia", DAY.plusDays(1), START, null));
    }

    @Test
    @DisplayName("Excluye el propio evento al editar")
    void excludesTheEventBeingEdited() {
        Event event = save("Feria de Ciencia", START, true);

        assertDoesNotThrow(() -> guard.ensureNoPublishedDuplicate(
                "Feria de Ciencia", DAY, START, event.getId()));
    }

    @Test
    @DisplayName("Encuentra los posibles duplicados no archivados")
    void findsPossibleDuplicates() {
        Event first = save("Feria de Ciencia", START, false);
        save("feria de ciencia", START, true);

        List<Event> duplicates = guard.findPossibleDuplicates(first);

        assertEquals(1, duplicates.size());
        assertEquals(Status.PUBLISHED, duplicates.get(0).getStatus());
    }
}
