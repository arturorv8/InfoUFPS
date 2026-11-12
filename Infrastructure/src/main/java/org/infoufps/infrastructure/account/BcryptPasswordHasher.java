package org.infoufps.infrastructure.account;

import jakarta.enterprise.context.ApplicationScoped;
import org.infoufps.application.account.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {

    private static final int LOG_ROUNDS = 12; // costo de BCrypt

    @Override
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula ni vacía");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean verify(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(password, hashedPassword);
    }
}
