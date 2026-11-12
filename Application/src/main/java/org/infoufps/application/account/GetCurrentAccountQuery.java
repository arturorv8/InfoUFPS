package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Consulta el perfil de la cuenta autenticada.
 */
@ApplicationScoped
public class GetCurrentAccountQuery {

    private final IAccountRepository accountRepository;

    @Inject
    public GetCurrentAccountQuery(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDto execute(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new NotFoundException("La cuenta no existe");
        }
        return accountRepository.findById(AccountId.of(accountId))
                .map(AccountDto::from)
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));
    }
}
