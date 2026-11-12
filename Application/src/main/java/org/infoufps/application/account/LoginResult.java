package org.infoufps.application.account;

import org.infoufps.domain.account.Account;

/**
 * Resultado de una autenticación exitosa.
 *
 * @param accessToken          token JWT que el cliente debe enviar como Bearer.
 * @param tokenType            esquema de autorización ("Bearer").
 * @param expiresIn            vigencia del token de acceso en segundos.
 * @param refreshToken         token opaco para renovar la sesión.
 * @param refreshExpiresIn     vigencia del token de renovación en segundos.
 * @param account              datos públicos de la cuenta autenticada.
 */
public record LoginResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        AccountDto account
) {
    static LoginResult from(IssuedSession session, Account account) {
        return new LoginResult(
                session.accessToken().token(),
                "Bearer",
                session.accessToken().expiresInSeconds(),
                session.refreshToken(),
                session.refreshExpiresInSeconds(),
                AccountDto.from(account)
        );
    }
}
