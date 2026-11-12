package org.infoufps.domain.account;

/**
 * Estado del ciclo de vida de una cuenta.
 * <p>
 * Una cuenta {@link #ACTIVE} puede autenticarse y operar con normalidad. Una
 * cuenta {@link #SUSPENDED} conserva sus datos pero no puede iniciar sesión ni
 * usar los recursos protegidos.
 */
public enum AccountStatus {
    ACTIVE,
    SUSPENDED
}
