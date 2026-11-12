package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.account.RefreshToken;

/**
 * Emite un par de tokens (acceso + renovación) y persiste el hash del token de
 * renovación. La rotación reutiliza este servicio para emitir el token nuevo.
 */
@ApplicationScoped
public class SessionTokenIssuer {

    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final IRefreshTokenRepository refreshTokenRepository;

    @Inject
    public SessionTokenIssuer(AccessTokenIssuer accessTokenIssuer,
                              RefreshTokenGenerator refreshTokenGenerator,
                              IRefreshTokenRepository refreshTokenRepository) {
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public IssuedSession issue(Account account) {
        IssuedAccessToken accessToken = accessTokenIssuer.issue(account);
        GeneratedRefreshToken generated = refreshTokenGenerator.generate();
        RefreshToken refreshToken = RefreshToken.issue(
                account.getAccountId(),
                generated.tokenHash(),
                generated.expiresInSeconds()
        );
        refreshTokenRepository.save(refreshToken);
        return new IssuedSession(accessToken, generated.rawToken(), generated.expiresInSeconds());
    }
}
