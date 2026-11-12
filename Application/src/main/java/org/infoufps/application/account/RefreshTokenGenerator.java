package org.infoufps.application.account;

/**
 * Puerto de salida para generar tokens opacos de renovación y calcular su hash.
 * La implementación concreta usa criptografía segura y una sal aleatoria.
 */
public interface RefreshTokenGenerator {

    GeneratedRefreshToken generate();

    String hash(String rawToken);
}
