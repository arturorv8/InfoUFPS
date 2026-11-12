package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Actualiza el nombre visible de la cuenta autenticada.
 */
@ApplicationScoped
public class UpdateAccountNameHandler {

    private final IAccountRepository accountRepository;

    @Inject
    public UpdateAccountNameHandler(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountDto execute(String accountId, String newName) {
        Account account = accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));
        account.changeName(newName);
        return AccountDto.from(accountRepository.save(account));
    }
}
