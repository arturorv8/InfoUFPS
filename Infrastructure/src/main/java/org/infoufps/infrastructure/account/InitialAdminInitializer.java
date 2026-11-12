package org.infoufps.infrastructure.account;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.account.PasswordHasher;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.valueobjects.Email;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Garantiza la existencia de una cuenta administradora inicial a partir de
 * variables de entorno. Si las credenciales no están configuradas, no hace nada.
 * <p>
 * Nunca registra la contraseña ni su hash.
 */
@ApplicationScoped
public class InitialAdminInitializer {

    private static final Logger LOG = Logger.getLogger(InitialAdminInitializer.class);

    private final IAccountRepository accountRepository;
    private final PasswordHasher passwordHasher;

    @ConfigProperty(name = "account.initial-admin.email")
    Optional<String> email;

    @ConfigProperty(name = "account.initial-admin.password")
    Optional<String> password;

    @ConfigProperty(name = "account.initial-admin.name", defaultValue = "Administrador")
    String name;

    @Inject
    public InitialAdminInitializer(IAccountRepository accountRepository, PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        String configuredEmail = email.orElse("");
        String configuredPassword = password.orElse("");
        if (configuredEmail.isBlank() || configuredPassword.isBlank()) {
            return;
        }

        String normalizedEmail = Email.normalize(configuredEmail);
        if (normalizedEmail == null) {
            LOG.warn("La cuenta administradora inicial no se creó: el email configurado no es válido");
            return;
        }

        Account existing = accountRepository.findByEmail(normalizedEmail).orElse(null);
        if (existing == null) {
            Account admin = Account.register(
                    normalizedEmail,
                    passwordHasher.hash(configuredPassword),
                    name,
                    Role.ADMIN
            );
            accountRepository.save(admin);
            LOG.info("Cuenta administradora inicial creada: " + normalizedEmail);
            return;
        }

        boolean changed = false;
        if (existing.getRole() != Role.ADMIN) {
            existing.changeRole(Role.ADMIN);
            changed = true;
        }
        if (!existing.isActive()) {
            existing.activate();
            changed = true;
        }
        if (changed) {
            accountRepository.save(existing);
            LOG.info("Cuenta administradora inicial actualizada: " + normalizedEmail);
        }
    }
}
