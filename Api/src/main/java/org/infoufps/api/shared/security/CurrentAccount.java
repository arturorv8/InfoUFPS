package org.infoufps.api.shared.security;

import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;

/**
 * Identidad de la cuenta autenticada en la petición actual.
 * <p>
 * El rol nunca se toma del cliente: se resuelve contra el repositorio a partir
 * del sujeto del token, de modo que los cambios de rol y las suspensiones
 * surten efecto sin esperar a que venza el token.
 *
 * @param accountId     identificador de la cuenta; puede ser null si no hay token.
 * @param admin         true si el rol actual es administrador.
 * @param emailVerified true si la cuenta tiene el correo verificado.
 */
public record CurrentAccount(String accountId, boolean admin, boolean emailVerified) {

    public CurrentAccount(String accountId, boolean admin) {
        this(accountId, admin, true);
    }

    /**
     * Identidad sin cuenta autenticada.
     */
    public static CurrentAccount anonymous() {
        return new CurrentAccount(null, false, false);
    }

    /**
     * Exige una cuenta autenticada.
     *
     * @return identificador de la cuenta.
     * @throws UnauthorizedException si no llegó un token válido.
     */
    public String requireAccountId() {
        if (accountId == null || accountId.isBlank()) {
            throw new UnauthorizedException("Se requiere una cuenta autenticada");
        }
        return accountId;
    }

    /**
     * Exige el rol de administrador. Primero valida que exista una cuenta para
     * que una petición sin identidad produzca 401 y una cuenta sin permisos 403.
     *
     * @throws UnauthorizedException si no llegó un token válido.
     * @throws ForbiddenException    si el rol no es administrador.
     */
    public void requireAdmin() {
        requireAccountId();
        if (!admin) {
            throw new ForbiddenException("Se requiere rol de administrador");
        }
    }

    /**
     * Exige que la cuenta esté autenticada y tenga el correo electrónico verificado.
     *
     * @throws UnauthorizedException si no llegó un token válido.
     * @throws ForbiddenException    si el email no está verificado.
     */
    public void requireEmailVerified() {
        requireAccountId();
        if (!emailVerified) {
            throw new ForbiddenException("Se requiere una cuenta con email verificado");
        }
    }
}
