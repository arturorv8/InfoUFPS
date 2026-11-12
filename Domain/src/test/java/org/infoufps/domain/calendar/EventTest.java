package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.EventPhase;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Event: reglas de negocio del calendario")
class EventTest {

    private static final AccountId AUTHOR = AccountId.of("account-1");
    private static final AccountId OTHER = AccountId.of("account-2");

    private static EventSchedule futureSchedule() {
        LocalDate day = BogotaTime.today().plusDays(10);
        return EventSchedule.of(day, day, LocalTime.of(8, 0), LocalTime.of(10, 0));
    }

    private static Event newEvent(boolean admin) {
        return Event.create("Feria de ciencia", EventType.ACADEMICO, "Bloque A",
                futureSchedule(), "Descripción", "https://ufps.edu.co/evento", AUTHOR, admin);
    }

    private static Event persistedEvent(Status status, EventSchedule schedule) {
        LocalDateTime now = BogotaTime.now();
        return Event.of(EventId.generate(), "Evento", EventType.GENERAL, "Auditorio",
                schedule, null, null, status, AUTHOR, true, null, now, now, 3);
    }

    @Test
    @DisplayName("Un usuario crea una propuesta en revisión")
    void userCreatesProposalUnderReview() {
        Event event = newEvent(false);

        assertEquals(Status.UNDER_REVIEW, event.getStatus());
        assertEquals(EventType.ACADEMICO, event.getType());
        assertEquals(AUTHOR, event.getProposedBy());
        assertFalse(event.isCreatedByAdmin());
        assertEquals(0, event.getRevision());
        assertNotNull(event.getCreatedAt());
        assertNull(event.getPublishedAt());
    }

    @Test
    @DisplayName("Un tipo nulo se normaliza a GENERAL")
    void nullTypeDefaultsToGeneral() {
        Event event = Event.create("Charla", null, "Sala 1",
                futureSchedule(), null, null, AUTHOR, false);

        assertEquals(EventType.GENERAL, event.getType());
    }

    @Test
    @DisplayName("El título y el lugar son obligatorios")
    void titleAndLocationAreRequired() {
        assertThrows(DomainException.class, () -> Event.create(
                "  ", EventType.GENERAL, "Sala", futureSchedule(), null, null, AUTHOR, false));
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, null, futureSchedule(), null, null, AUTHOR, false));
    }

    @Test
    @DisplayName("El título y el lugar respetan la longitud máxima")
    void textLengthIsLimited() {
        assertThrows(DomainException.class, () -> Event.create(
                "x".repeat(Event.MAX_TITLE_LENGTH + 1), EventType.GENERAL, "Sala",
                futureSchedule(), null, null, AUTHOR, false));
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, "x".repeat(Event.MAX_LOCATION_LENGTH + 1),
                futureSchedule(), null, null, AUTHOR, false));
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, "Sala", futureSchedule(),
                "x".repeat(Event.MAX_DESCRIPTION_LENGTH + 1), null, AUTHOR, false));
    }

    @Test
    @DisplayName("El título y el lugar se recortan")
    void textIsTrimmed() {
        Event event = Event.create("  Charla  ", EventType.GENERAL, "  Sala 1  ",
                futureSchedule(), "  detalle  ", null, AUTHOR, false);

        assertEquals("Charla", event.getTitle());
        assertEquals("Sala 1", event.getLocation());
        assertEquals("detalle", event.getDescription());
    }

    @Test
    @DisplayName("Solo se aceptan URLs http o https")
    void infoUrlMustBeHttpOrHttps() {
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, "Sala", futureSchedule(), null,
                "ftp://example.com", AUTHOR, false));
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, "Sala", futureSchedule(), null,
                "no-es-url", AUTHOR, false));

        Event event = Event.create("Título", EventType.GENERAL, "Sala", futureSchedule(),
                null, "https://example.com/info", AUTHOR, false);
        assertEquals("https://example.com/info", event.getInfoUrl());
    }

    @Test
    @DisplayName("Los tipos INSTITUCIONAL y CALENDARIO_ACADEMICO son solo para administradores")
    void restrictedTypesRequireAdmin() {
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.INSTITUCIONAL, "Sala", futureSchedule(), null, null, AUTHOR, false));
        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.CALENDARIO_ACADEMICO, "Sala", futureSchedule(), null, null, AUTHOR, false));

        Event adminEvent = Event.create("Título", EventType.INSTITUCIONAL, "Sala",
                futureSchedule(), null, null, AUTHOR, true);
        assertEquals(EventType.INSTITUCIONAL, adminEvent.getType());
        assertTrue(adminEvent.isCreatedByAdmin());
    }

    @Test
    @DisplayName("Un usuario no puede proponer fechas pasadas; un administrador sí")
    void pastDatesAreForbiddenForUsersOnly() {
        LocalDate yesterday = BogotaTime.today().minusDays(1);
        EventSchedule past = EventSchedule.of(yesterday, yesterday, null, null);

        assertThrows(DomainException.class, () -> Event.create(
                "Título", EventType.GENERAL, "Sala", past, null, null, AUTHOR, false));

        Event adminEvent = Event.create("Título", EventType.GENERAL, "Sala",
                past, null, null, AUTHOR, true);
        assertEquals(yesterday, adminEvent.getStartDate());
    }

    @Test
    @DisplayName("Publicar cambia el estado, fija la fecha de publicación y actualiza updatedAt")
    void publishSetsStatusAndPublishedAt() {
        Event event = newEvent(false);
        LocalDateTime beforePublish = event.getUpdatedAt();

        event.publish();

        assertEquals(Status.PUBLISHED, event.getStatus());
        assertNotNull(event.getPublishedAt());
        assertNotNull(event.getUpdatedAt());
        assertFalse(event.getUpdatedAt().isBefore(beforePublish));
        assertThrows(DomainException.class, event::publish);
    }

    @Test
    @DisplayName("Archivar cambia el estado, actualiza updatedAt y no se puede volver a publicar")
    void archivedEventCannotBePublished() {
        Event event = newEvent(true);
        event.publish();
        LocalDateTime beforeArchive = event.getUpdatedAt();
        event.archive();

        assertEquals(Status.ARCHIVED, event.getStatus());
        assertNotNull(event.getUpdatedAt());
        assertFalse(event.getUpdatedAt().isBefore(beforeArchive));
        assertThrows(DomainException.class, event::publish);
    }

    @Test
    @DisplayName("Un usuario solo edita su propia propuesta en revisión")
    void userCanOnlyEditOwnProposalInReview() {
        Event event = newEvent(false);

        assertThrows(ForbiddenException.class, () -> event.update(
                "Nuevo", EventType.GENERAL, "Sala", futureSchedule(), null, null, OTHER, false));

        event.publish();
        assertThrows(DomainException.class, () -> event.update(
                "Nuevo", EventType.GENERAL, "Sala", futureSchedule(), null, null, AUTHOR, false));
    }

    @Test
    @DisplayName("Editar incrementa la revisión y conserva la fecha de publicación")
    void updateIncrementsRevisionAndKeepsPublishedAt() {
        Event event = newEvent(true);
        event.publish();
        LocalDateTime publishedAt = event.getPublishedAt();

        event.update("Nuevo título", EventType.CULTURAL, "Nuevo lugar",
                futureSchedule(), "Nueva descripción", null, AUTHOR, true);

        assertEquals("Nuevo título", event.getTitle());
        assertEquals(EventType.CULTURAL, event.getType());
        assertEquals(1, event.getRevision());
        assertEquals(publishedAt, event.getPublishedAt());
    }

    @Test
    @DisplayName("Un evento archivado no puede editarse")
    void archivedEventCannotBeUpdated() {
        Event event = newEvent(true);
        event.archive();

        assertThrows(DomainException.class, () -> event.update(
                "Nuevo", EventType.GENERAL, "Sala", futureSchedule(), null, null, AUTHOR, true));
    }

    @Test
    @DisplayName("Un administrador puede editar cualquier evento no archivado")
    void adminCanEditAnyEvent() {
        Event event = newEvent(false);

        event.update("Corregido", EventType.GENERAL, "Sala", futureSchedule(),
                null, null, OTHER, true);

        assertEquals("Corregido", event.getTitle());
        assertEquals(1, event.getRevision());
    }

    @Test
    @DisplayName("La fase incluye el primer y el último día del evento")
    void phaseBoundaries() {
        LocalDate start = LocalDate.of(2026, 5, 10);
        EventSchedule schedule = EventSchedule.of(start, start.plusDays(2), null, null);
        Event event = persistedEvent(Status.PUBLISHED, schedule);

        assertEquals(EventPhase.UPCOMING, event.phase(start.minusDays(1)));
        assertEquals(EventPhase.ONGOING, event.phase(start));
        assertEquals(EventPhase.ONGOING, event.phase(start.plusDays(1)));
        assertEquals(EventPhase.ONGOING, event.phase(start.plusDays(2)));
        assertEquals(EventPhase.FINISHED, event.phase(start.plusDays(3)));
    }

    @Test
    @DisplayName("Los días restantes cuentan hasta el inicio o hasta el último día")
    void daysRemainingBoundaries() {
        LocalDate start = LocalDate.of(2026, 5, 10);
        EventSchedule schedule = EventSchedule.of(start, start.plusDays(3), null, null);
        Event event = persistedEvent(Status.PUBLISHED, schedule);

        assertEquals(5, event.daysRemaining(start.minusDays(5)));
        assertEquals(3, event.daysRemaining(start));
        assertEquals(2, event.daysRemaining(start.plusDays(1)));
        assertEquals(0, event.daysRemaining(start.plusDays(3)));
        assertEquals(0, event.daysRemaining(start.plusDays(4)));
    }
}
