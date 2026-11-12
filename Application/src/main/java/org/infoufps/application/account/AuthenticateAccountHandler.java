package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;
import org.infoufps.domain.shared.valueobjects.Email;

/**
 * Caso de uso de inicio de sesión.
 * <p>
 * No revela si el email existe o si la contraseña es incorrecta: ambos casos
 * producen el mismo error 401. Una cuenta suspendida, con credenciales válidas,
 * produce 403.
 */
@ApplicationScoped
public class AuthenticateAccountHandler {

    private final IAccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final SessionTokenIssuer sessionTokenIssuer;

    @Inject
    public AuthenticateAccountHandler(IAccountRepository accountRepository,
                                      PasswordHasher passwordHasher,
                                      SessionTokenIssuer sessionTokenIssuer) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.sessionTokenIssuer = sessionTokenIssuer;
    }

    public LoginResult execute(LoginCommand command) {
        String email = Email.normalize(command.email());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordHasher.verify(command.password(), account.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (!account.isActive()) {
            throw new ForbiddenException("La cuenta está suspendida");
        }

        IssuedSession session = sessionTokenIssuer.issue(account);
        return LoginResult.from(session, account);
    }
}
