package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.shared.time.BogotaTime;

/**
 * Cierra una sesión revocando el token de renovación presentado. La operación
 * es idempotente: un token ausente o ya revocado no produce error.
 */
@ApplicationScoped
public class LogoutHandler {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Inject
    public LogoutHandler(IRefreshTokenRepository refreshTokenRepository,
                         RefreshTokenGenerator refreshTokenGenerator) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = refreshTokenGenerator.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke(BogotaTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }
}
