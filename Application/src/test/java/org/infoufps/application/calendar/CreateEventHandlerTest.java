package org.infoufps.application.calendar;

import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CreateEventHandler: creación administrativa")
class CreateEventHandlerTest {

    private static final String ADMIN = "admin-1";

    private InMemoryEventRepository repository;
    private CreateEventHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        handler = new CreateEventHandler(repository, new EventDuplicateGuard(repository),
                new EventDtoAssembler());
    }

    private static EventRequest request(String title, LocalDate start, String type) {
        return new EventRequest(title, type, "Bloque A", start, start,
                LocalTime.of(8, 0), LocalTime.of(10, 0), null, null);
    }

    @Test
    @DisplayName("El administrador crea el evento ya publicado")
    void createsPublishedEvent() {
        EventDto dto = handler.execute(new CreateEventCommand(
                request("Feria de ciencia", BogotaTime.today().plusDays(5), "ACADEMICO")), ADMIN);

        assertEquals(Status.PUBLISHED, dto.status());
        assertNotNull(dto.publishedAt());
        assertEquals("Admin", dto.authorName());
        assertEquals(1, repository.size());
    }

    @Test
    @DisplayName("El administrador puede usar tipos restringidos")
    void adminCanUseRestrictedTypes() {
        EventDto dto = handler.execute(new CreateEventCommand(
                request("Ceremonia", BogotaTime.today().plusDays(5), "INSTITUCIONAL")), ADMIN);

        assertEquals(EventType.INSTITUCIONAL, dto.type());
    }

    @Test
    @DisplayName("El administrador puede crear eventos con fecha pasada")
    void adminCanCreatePastEvents() {
        EventDto dto = handler.execute(new CreateEventCommand(
                request("Histórico", BogotaTime.today().minusDays(5), "GENERAL")), ADMIN);

        assertEquals(BogotaTime.today().minusDays(5), dto.startDate());
    }

    @Test
    @DisplayName("Rechaza un duplicado publicado")
    void rejectsPublishedDuplicate() {
        LocalDate day = BogotaTime.today().plusDays(5);
        handler.execute(new CreateEventCommand(request("Feria", day, "ACADEMICO")), ADMIN);

        assertThrows(ConflictException.class, () -> handler.execute(
                new CreateEventCommand(request("FERIA", day, "ACADEMICO")), ADMIN));
    }

    @Test
    @DisplayName("La cuenta administradora es obligatoria")
    void adminAccountIsRequired() {
        assertThrows(org.infoufps.domain.shared.exceptions.DomainException.class, () -> handler.execute(
                new CreateEventCommand(request("Feria", BogotaTime.today().plusDays(5), "GENERAL")), " "));
    }
}
