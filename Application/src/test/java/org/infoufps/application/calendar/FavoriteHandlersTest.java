package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.calendar.PublishedEventLoader;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.application.calendar.InMemoryFavoriteEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Favoritos: alta, baja e idempotencia")
class FavoriteHandlersTest {

    private static final String ACCOUNT = "account-1";

    private InMemoryEventRepository eventRepository;
    private InMemoryFavoriteEventRepository favoriteRepository;
    private AddFavoriteHandler addHandler;
    private RemoveFavoriteHandler removeHandler;

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
        favoriteRepository = new InMemoryFavoriteEventRepository(eventRepository.snapshot());
        addHandler = new AddFavoriteHandler(favoriteRepository, new PublishedEventLoader(eventRepository));
        removeHandler = new RemoveFavoriteHandler(favoriteRepository);
    }

    private Event save(boolean publish) {
        LocalDate day = BogotaTime.today().plusDays(5);
        Event event = Event.create("Feria de ciencia", EventType.ACADEMICO, "Bloque A",
                EventSchedule.of(day, day, null, null), null, null,
                AccountId.of("author"), true);
        if (publish) {
            event.publish();
        }
        return eventRepository.save(event);
    }

    @Test
    @DisplayName("Marca un evento publicado como favorito")
    void addsPublishedEvent() {
        Event event = save(true);

        addHandler.execute(new AddFavoriteCommand(ACCOUNT, event.getId().value()));

        assertEquals(1, favoriteRepository.size());
        assertEquals(1, favoriteRepository.countPublishedByAccount(AccountId.of(ACCOUNT)));
    }

    @Test
    @DisplayName("Marcar dos veces el mismo evento es idempotente")
    void addIsIdempotent() {
        Event event = save(true);
        AddFavoriteCommand command = new AddFavoriteCommand(ACCOUNT, event.getId().value());

        addHandler.execute(command);
        addHandler.execute(command);

        assertEquals(1, favoriteRepository.size());
    }

    @Test
    @DisplayName("No se puede marcar como favorito un evento no publicado")
    void cannotFavoriteUnpublishedEvent() {
        Event event = save(false);

        assertThrows(NotFoundException.class,
                () -> addHandler.execute(new AddFavoriteCommand(ACCOUNT, event.getId().value())));
    }

    @Test
    @DisplayName("Un evento inexistente produce 404")
    void unknownEventIsNotFound() {
        assertThrows(NotFoundException.class,
                () -> addHandler.execute(new AddFavoriteCommand(ACCOUNT, "no-existe")));
    }

    @Test
    @DisplayName("Quitar un favorito es idempotente")
    void removeIsIdempotent() {
        Event event = save(true);
        addHandler.execute(new AddFavoriteCommand(ACCOUNT, event.getId().value()));

        removeHandler.execute(new RemoveFavoriteCommand(ACCOUNT, event.getId().value()));
        assertDoesNotThrow(() ->
                removeHandler.execute(new RemoveFavoriteCommand(ACCOUNT, event.getId().value())));

        assertEquals(0, favoriteRepository.size());
    }

    @Test
    @DisplayName("Al archivar se conserva el favorito pero deja de listarse")
    void archivedEventKeepsFavoriteButHidesIt() {
        Event event = save(true);
        addHandler.execute(new AddFavoriteCommand(ACCOUNT, event.getId().value()));

        event.archive();

        assertEquals(1, favoriteRepository.size());
        assertEquals(0, favoriteRepository.findPublishedEventIdsByAccount(AccountId.of(ACCOUNT)).size());
    }
}
