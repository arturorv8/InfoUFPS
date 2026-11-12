package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.time.BogotaTime;

/**
 * Suspende una cuenta y revoca sus sesiones de renovación.
 */
@ApplicationScoped
public class SuspendAccountHandler {

    private final IAccountRepository accountRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final AccountAdminGuard adminGuard;

    @Inject
    public SuspendAccountHandler(IAccountRepository accountRepository,
                                 IRefreshTokenRepository refreshTokenRepository,
                                 AccountAdminGuard adminGuard) {
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.adminGuard = adminGuard;
    }

    @Transactional
    public AccountDto execute(String accountId, String actingAccountId) {
        Account target = accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));

        adminGuard.ensureCanSuspend(target, actingAccountId);

        target.suspend();
        Account saved = accountRepository.save(target);
        refreshTokenRepository.revokeAllByAccountId(target.getAccountId(), BogotaTime.now());
        return AccountDto.from(saved);
    }
}
