package org.infoufps.api.shared.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.infoufps.application.account.AccessTokenVerifier;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;

/**
 * Contexto de identidad de la petición.
 * <p>
 * La identidad se toma del token Bearer de la cabecera {@code Authorization}.
 * El rol y el estado nunca se toman del cliente: se resuelven contra
 * {@link IAccountRepository} a partir del sujeto del token. Así, un cambio de
 * rol o una suspensión surten efecto de inmediato.
 */
@RequestScoped
public class ActorContext {

    @Context
    HttpHeaders httpHeaders;

    @Inject
    AccessTokenVerifier accessTokenVerifier;

    @Inject
    IAccountRepository accountRepository;

    /**
     * @return identidad normalizada de la petición. Nunca contiene un rol
     * proporcionado por el cliente.
     */
    public CurrentAccount currentAccount() {
        String token = bearerToken();
        if (token == null) {
            return CurrentAccount.anonymous();
        }

        String accountId = accessTokenVerifier.verify(token);
        return accountRepository.findById(AccountId.of(accountId))
                .filter(Account::isActive)
                .map(account -> new CurrentAccount(
                        account.getAccountId().value(),
                        account.getRole() == Role.ADMIN,
                        account.isEmailVerified()))
                .orElseGet(CurrentAccount::anonymous);
    }

    private String bearerToken() {
        String header = httpHeaders != null
                ? httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION)
                : null;
        if (header == null || header.isBlank()) {
            return null;
        }
        String value = header.strip();
        if (!value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = value.substring(7).strip();
        return token.isEmpty() ? null : token;
    }
}
