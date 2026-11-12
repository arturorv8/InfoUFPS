package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Reactiva una cuenta suspendida. La operación es idempotente.
 */
@ApplicationScoped
public class ActivateAccountHandler {

    private final IAccountRepository accountRepository;

    @Inject
    public ActivateAccountHandler(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountDto execute(String accountId) {
        Account target = accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));

        target.activate();
        return AccountDto.from(accountRepository.save(target));
    }
}
