package org.infoufps.api.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.application.account.AccessTokenVerifier;
import org.infoufps.application.account.IssuedAccessToken;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Emisor y verificador de tokens JWT firmados con HMAC-SHA256.
 * <p>
 * El token solo transporta el sujeto (identificador de cuenta) y metadatos
 * estándar. El rol se resuelve siempre contra la base de datos en
 * {@link ActorContext}. La firma se compara en tiempo constante.
 */
@ApplicationScoped
public class JwtAccessTokenService implements AccessTokenIssuer, AccessTokenVerifier {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String JWT_ALG = "HS256";
    private static final int MIN_SECRET_BYTES = 32;
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ConfigProperty(name = "account.jwt.secret", defaultValue = "")
    String secret;

    @ConfigProperty(name = "account.jwt.issuer", defaultValue = "infoufps")
    String issuer;

    @ConfigProperty(name = "account.jwt.access-token-ttl-seconds", defaultValue = "3600")
    long accessTokenTtlSeconds;

    private byte[] secretBytes;

    @PostConstruct
    void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "account.jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " bytes");
        }
        secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public IssuedAccessToken issue(Account account) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenTtlSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", JWT_ALG);
        header.put("typ", "JWT");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", account.getAccountId().value());
        claims.put("role", account.getRole().name());
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());
        claims.put("jti", UUID.randomUUID().toString());

        String encodedHeader = encodeJson(header);
        String encodedClaims = encodeJson(claims);
        String signingInput = encodedHeader + "." + encodedClaims;
        String signature = ENCODER.encodeToString(sign(signingInput));

        return new IssuedAccessToken(signingInput + "." + signature, accessTokenTtlSeconds);
    }

    @Override
    public String verify(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.");
            if (parts.length != 3) {
                throw invalidToken();
            }

            Map<String, Object> header = decodeJson(parts[0]);
            if (!JWT_ALG.equals(header.get("alg"))) {
                throw invalidToken();
            }

            byte[] expected = sign(parts[0] + "." + parts[1]);
            byte[] provided = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, provided)) {
                throw invalidToken();
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            if (!issuer.equals(claims.get("iss"))) {
                throw invalidToken();
            }

            Object expiration = claims.get("exp");
            if (!(expiration instanceof Number number)
                    || Instant.now().getEpochSecond() >= number.longValue()) {
                throw invalidToken();
            }

            Object subject = claims.get("sub");
            if (!(subject instanceof String accountId) || accountId.isBlank()) {
                throw invalidToken();
            }
            return accountId;
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalidToken();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return ENCODER.encodeToString(MAPPER.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo serializar el token", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJson(String value) {
        try {
            return MAPPER.readValue(DECODER.decode(value), Map.class);
        } catch (Exception exception) {
            throw invalidToken();
        }
    }

    private byte[] sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, ALGORITHM));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar el token", exception);
        }
    }

    private UnauthorizedException invalidToken() {
        return new UnauthorizedException("Token inválido o vencido");
    }
}
