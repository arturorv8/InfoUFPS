package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Cambia el rol de una cuenta. El nuevo rol surte efecto de inmediato porque
 * {@code ActorContext} resuelve el rol contra base de datos en cada petición.
 */
@ApplicationScoped
public class ChangeAccountRoleHandler {

    private final IAccountRepository accountRepository;
    private final AccountAdminGuard adminGuard;

    @Inject
    public ChangeAccountRoleHandler(IAccountRepository accountRepository,
                                    AccountAdminGuard adminGuard) {
        this.accountRepository = accountRepository;
        this.adminGuard = adminGuard;
    }

    @Transactional
    public AccountDto execute(String accountId, Role newRole, String actingAccountId) {
        Account target = accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));

        adminGuard.ensureCanChangeRole(target, newRole, actingAccountId);

        target.changeRole(newRole);
        return AccountDto.from(accountRepository.save(target));
    }
}
