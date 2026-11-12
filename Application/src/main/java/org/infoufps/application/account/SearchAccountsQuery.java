package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountSearchCriteria;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

/**
 * Búsqueda administrativa de cuentas con filtros y paginación.
 */
@ApplicationScoped
public class SearchAccountsQuery {

    private final IAccountRepository accountRepository;

    @Inject
    public SearchAccountsQuery(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public PageResult<AccountDto> execute(AccountSearchCriteria criteria, PageRequest pageRequest) {
        AccountSearchCriteria effective = criteria != null
                ? criteria
                : new AccountSearchCriteria(null, null, null);
        PageRequest page = pageRequest != null ? pageRequest : PageRequest.first();
        return accountRepository.search(effective, page).map(AccountDto::from);
    }
}
