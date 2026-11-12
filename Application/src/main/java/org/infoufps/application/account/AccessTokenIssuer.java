package org.infoufps.application.account;

import org.infoufps.domain.account.Account;

/**
 * Puerto de salida para emitir tokens de acceso firmados.
 */
public interface AccessTokenIssuer {

    IssuedAccessToken issue(Account account);
}
