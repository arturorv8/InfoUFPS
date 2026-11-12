package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.account.RefreshToken;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;

/**
 * Renueva una sesión rotando el token de renovación.
 * <p>
 * Cada token sirve una sola vez. Si se presenta un token ya revocado, se
 * interpreta como reutilización y se revocan todos los tokens de la cuenta.
 */
@ApplicationScoped
public class RefreshSessionHandler {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IAccountRepository accountRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final SessionTokenIssuer sessionTokenIssuer;
    private final RefreshTokenRevocationService revocationService;

    @Inject
    public RefreshSessionHandler(IRefreshTokenRepository refreshTokenRepository,
                                 IAccountRepository accountRepository,
                                 RefreshTokenGenerator refreshTokenGenerator,
                                 SessionTokenIssuer sessionTokenIssuer,
                                 RefreshTokenRevocationService revocationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountRepository = accountRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.sessionTokenIssuer = sessionTokenIssuer;
        this.revocationService = revocationService;
    }

    @Transactional
    public LoginResult execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw invalidToken();
        }

        String tokenHash = refreshTokenGenerator.hash(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::invalidToken);

        LocalDateTime now = BogotaTime.now();

        if (token.isRevoked()) {
            revocationService.revokeAllForAccount(token.getAccountId());
            throw new UnauthorizedException("Token de renovación reutilizado");
        }

        if (token.isExpired(now)) {
            throw invalidToken();
        }

        // Revocación atómica: si otra petición ganó la carrera, se trata como reutilización.
        if (!refreshTokenRepository.revokeIfActive(token.getTokenId(), now)) {
            revocationService.revokeAllForAccount(token.getAccountId());
            throw new UnauthorizedException("Token de renovación reutilizado");
        }

        Account account = accountRepository.findById(token.getAccountId())
                .orElseThrow(this::invalidToken);

        if (!account.isActive()) {
            revocationService.revokeAllForAccount(account.getAccountId());
            throw new ForbiddenException("La cuenta está suspendida");
        }

        IssuedSession session = sessionTokenIssuer.issue(account);
        return LoginResult.from(session, account);
    }

    private UnauthorizedException invalidToken() {
        return new UnauthorizedException("Token de renovación inválido o vencido");
    }
}
