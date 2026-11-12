package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PublishEventHandler: publicación de eventos")
class PublishEventHandlerTest {

    private static final AccountId AUTHOR = AccountId.of("account-1");
    private static final LocalDate DAY = BogotaTime.today().plusDays(10);
    private static final LocalTime START = LocalTime.of(8, 0);

    private InMemoryEventRepository repository;
    private PublishEventHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        handler = new PublishEventHandler(repository, new EventDtoAssembler(),
                new EventDuplicateGuard(repository));
    }

    private Event save(String title, boolean published) {
        Event event = Event.create(title, EventType.ACADEMICO, "Bloque A",
                EventSchedule.of(DAY, DAY, START, START.plusHours(2)),
                null, null, AUTHOR, false);
        if (published) {
            event.publish();
        }
        return repository.save(event);
    }

    @Test
    @DisplayName("No publica un evento que duplica a uno ya publicado")
    void rejectsPublishedDuplicate() {
        save("Feria de Ciencia", true);
        Event proposal = save("feria de ciencia", false);

        assertThrows(ConflictException.class,
                () -> handler.execute(new PublishEventCommand(proposal.getId().value())));
        assertEquals(Status.UNDER_REVIEW,
                repository.findById(proposal.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Publica un evento sin duplicados")
    void publishesWhenNoDuplicateExists() {
        Event proposal = save("Feria de Ciencia", false);

        var dto = handler.execute(new PublishEventCommand(proposal.getId().value()));

        assertEquals(Status.PUBLISHED, dto.status());
        assertEquals(Status.PUBLISHED,
                repository.findById(proposal.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Un evento inexistente responde 404")
    void rejectsUnknownEvent() {
        assertThrows(NotFoundException.class,
                () -> handler.execute(new PublishEventCommand("no-existe")));
    }
}
