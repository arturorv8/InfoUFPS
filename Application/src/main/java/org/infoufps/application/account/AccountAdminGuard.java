package org.infoufps.application.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;

/**
 * Reglas que protegen la administración de cuentas: un administrador no puede
 * quitarse su propio rol, ni suspenderse a sí mismo, ni dejar el sistema sin
 * administradores activos.
 */
@ApplicationScoped
public class AccountAdminGuard {

    private final IAccountRepository accountRepository;

    @Inject
    public AccountAdminGuard(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void ensureCanChangeRole(Account target, Role newRole, String actingAccountId) {
        if (newRole == null) {
            throw new DomainException("El rol es obligatorio");
        }

        if (isSelf(target, actingAccountId) && newRole != Role.ADMIN) {
            throw new ForbiddenException("No puedes quitarte el rol de administrador");
        }

        boolean removesActiveAdmin = target.isActive()
                && target.getRole() == Role.ADMIN
                && newRole != Role.ADMIN;

        if (removesActiveAdmin && accountRepository.countActiveAdminsExcluding(target.getAccountId()) == 0) {
            throw new ConflictException("El sistema debe conservar al menos un administrador activo");
        }
    }

    public void ensureCanSuspend(Account target, String actingAccountId) {
        if (isSelf(target, actingAccountId)) {
            throw new ForbiddenException("No puedes suspender tu propia cuenta");
        }

        boolean suspendsActiveAdmin = target.isActive() && target.getRole() == Role.ADMIN;
        if (suspendsActiveAdmin && accountRepository.countActiveAdminsExcluding(target.getAccountId()) == 0) {
            throw new ConflictException("El sistema debe conservar al menos un administrador activo");
        }
    }

    private boolean isSelf(Account target, String actingAccountId) {
        return actingAccountId != null
                && target.getAccountId().value().equals(actingAccountId);
    }
}
