package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FavoriteEvent: identidad cuenta-evento")
class FavoriteEventTest {

    private static final AccountId ACCOUNT = AccountId.of("account-1");
    private static final EventId EVENT = EventId.of("event-1");

    @Test
    @DisplayName("La pareja cuenta-evento identifica al favorito")
    void equalityIsBasedOnAccountAndEvent() {
        FavoriteEvent first = FavoriteEvent.create(ACCOUNT, EVENT);
        FavoriteEvent second = FavoriteEvent.of(ACCOUNT, EVENT, first.getCreatedAt());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotNull(first.getCreatedAt());
    }

    @Test
    @DisplayName("Dos cuentas distintas producen favoritos distintos")
    void differentAccountsAreNotEqual() {
        FavoriteEvent first = FavoriteEvent.create(ACCOUNT, EVENT);
        FavoriteEvent second = FavoriteEvent.create(AccountId.of("account-2"), EVENT);

        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("La cuenta y el evento son obligatorios")
    void accountAndEventAreRequired() {
        assertThrows(NullPointerException.class, () -> FavoriteEvent.create(null, EVENT));
        assertThrows(NullPointerException.class, () -> FavoriteEvent.create(ACCOUNT, null));
        assertThrows(DomainException.class, () -> AccountId.of(" "));
    }
}
