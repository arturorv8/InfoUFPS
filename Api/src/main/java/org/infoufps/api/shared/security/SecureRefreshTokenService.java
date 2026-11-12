package org.infoufps.api.shared.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.account.GeneratedRefreshToken;
import org.infoufps.application.account.RefreshTokenGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Genera tokens opacos de renovación con {@link SecureRandom} y calcula su hash
 * SHA-256. El valor en claro nunca se persiste ni se registra en logs.
 */
@ApplicationScoped
public class SecureRefreshTokenService implements RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @ConfigProperty(name = "account.jwt.refresh-token-ttl-seconds", defaultValue = "2592000")
    long refreshTokenTtlSeconds;

    @Override
    public GeneratedRefreshToken generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String rawToken = ENCODER.encodeToString(bytes);
        return new GeneratedRefreshToken(rawToken, hash(rawToken), refreshTokenTtlSeconds);
    }

    @Override
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("El token de renovación es obligatorio");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo calcular el hash del token", exception);
        }
    }
}
