package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.time.BogotaTime;

/**
 * Cambia la contraseña verificando primero la actual y revoca todas las
 * sesiones de renovación de la cuenta. Los tokens de acceso vigentes siguen
 * funcionando hasta su vencimiento, pero el rol y el estado se consultan
 * siempre en base de datos.
 */
@ApplicationScoped
public class ChangePasswordHandler {

    private final IAccountRepository accountRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;

    @Inject
    public ChangePasswordHandler(IAccountRepository accountRepository,
                                 IRefreshTokenRepository refreshTokenRepository,
                                 PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void execute(String accountId, ChangePasswordCommand command) {
        Account account = accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new NotFoundException("La cuenta no existe"));

        if (!passwordHasher.verify(command.currentPassword(), account.getPasswordHash())) {
            throw new DomainException("La contraseña actual no es correcta");
        }

        account.changePasswordHash(passwordHasher.hash(command.newPassword()));
        accountRepository.save(account);
        refreshTokenRepository.revokeAllByAccountId(account.getAccountId(), BogotaTime.now());
    }
}
