package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.shared.time.BogotaTime;

/**
 * Revoca todos los tokens de renovación de una cuenta en una transacción
 * independiente. Se usa cuando se detecta reutilización de un token ya rotado:
 * la revocación debe persistir aunque la petición actual termine en error.
 */
@ApplicationScoped
public class RefreshTokenRevocationService {

    private final IRefreshTokenRepository refreshTokenRepository;

    @Inject
    public RefreshTokenRevocationService(IRefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void revokeAllForAccount(AccountId accountId) {
        refreshTokenRepository.revokeAllByAccountId(accountId, BogotaTime.now());
    }
}
