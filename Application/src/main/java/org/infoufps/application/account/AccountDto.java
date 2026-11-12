package org.infoufps.application.account;

import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.Role;

public record AccountDto(
        String accountId,
        String name,
        String email,
        Role role,
        AccountStatus status
) {
    public static AccountDto from(Account account) {
        return new AccountDto(
                account.getAccountId().value(),
                account.getName(),
                account.getEmail(),
                account.getRole(),
                account.getStatus()
        );
    }
}
