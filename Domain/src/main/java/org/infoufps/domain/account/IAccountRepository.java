package org.infoufps.domain.account;

import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IAccountRepository {

    Account save(Account account);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Account> findById(AccountId accountId);

    List<Account> findByIds(Collection<AccountId> accountIds);

    PageResult<Account> search(AccountSearchCriteria criteria, PageRequest pageRequest);

    /**
     * Cuenta los administradores activos distintos de la cuenta indicada.
     * Permite impedir que una operación deje el sistema sin administradores.
     */
    long countActiveAdminsExcluding(AccountId accountId);

    void deleteById(AccountId id);
}
