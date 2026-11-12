package org.infoufps.domain.account;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IRefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoca de forma atómica el token solo si sigue activo.
     *
     * @return {@code true} si esta llamada fue la que revocó el token; {@code false}
     *         si ya estaba revocado (posible reutilización o carrera).
     */
    boolean revokeIfActive(String tokenId, LocalDateTime revokedAt);

    void revokeAllByAccountId(AccountId accountId, LocalDateTime revokedAt);
}
