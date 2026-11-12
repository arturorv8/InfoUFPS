package org.infoufps.domain.account;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RefreshToken: vigencia y revocación")
class RefreshTokenTest {

    private static final AccountId ACCOUNT_ID = AccountId.generate();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 1, 12, 0);

    @Test
    @DisplayName("Un token recién emitido está activo")
    void newTokenIsActive() {
        RefreshToken token = RefreshToken.of("id", ACCOUNT_ID, "hash",
                NOW.minusDays(1), NOW.plusDays(1), null);

        assertTrue(token.isActive(NOW));
        assertFalse(token.isRevoked());
        assertFalse(token.isExpired(NOW));
    }

    @Test
    @DisplayName("Un token vencido no está activo")
    void expiredTokenIsNotActive() {
        RefreshToken token = RefreshToken.of("id", ACCOUNT_ID, "hash",
                NOW.minusDays(2), NOW.minusDays(1), null);

        assertTrue(token.isExpired(NOW));
        assertFalse(token.isActive(NOW));
    }

    @Test
    @DisplayName("Revocar es idempotente y conserva la primera fecha")
    void revokeIsIdempotent() {
        RefreshToken token = RefreshToken.of("id", ACCOUNT_ID, "hash",
                NOW.minusDays(1), NOW.plusDays(1), null);

        token.revoke(NOW);
        token.revoke(NOW.plusHours(1));

        assertTrue(token.isRevoked());
        assertEquals(NOW, token.getRevokedAt());
        assertFalse(token.isActive(NOW));
    }

    @Test
    @DisplayName("La vigencia debe ser positiva")
    void rejectsNonPositiveTtl() {
        assertThrows(DomainException.class,
                () -> RefreshToken.issue(ACCOUNT_ID, "hash", 0));
    }
}
