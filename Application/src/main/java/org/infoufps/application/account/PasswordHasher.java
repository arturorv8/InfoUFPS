package org.infoufps.application.account;

/**
 * Interfaz para hashing de contraseñas.
 * Permite inyectar la implementación real (BCrypt) o un fake para tests.
 */
public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hash);
}
