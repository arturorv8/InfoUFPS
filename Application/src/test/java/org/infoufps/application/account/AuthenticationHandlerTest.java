package org.infoufps.application.account;

import org.infoufps.application.ApplicationUseCasesTest;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Autenticación y consulta de la cuenta actual")
class AuthenticationHandlerTest {

    private static final String PASSWORD = "Secreto123!";

    private IAccountRepository accountRepository;
    private IRefreshTokenRepository refreshTokenRepository;
    private AuthenticateAccountHandler authenticateHandler;
    private GetCurrentAccountQuery currentAccountQuery;

    @BeforeEach
    void setUp() {
        accountRepository = new ApplicationUseCasesTest.InMemoryAccountRepository();
        refreshTokenRepository = new ApplicationUseCasesTest.InMemoryRefreshTokenRepository();

        PasswordHasher passwordHasher = new PasswordHasher() {
            @Override
            public String hash(String password) {
                return "hash:" + password;
            }

            @Override
            public boolean verify(String password, String hash) {
                return hash != null && hash.equals("hash:" + password);
            }
        };

        AccessTokenIssuer tokenIssuer = account ->
                new IssuedAccessToken("token-" + account.getAccountId().value(), 3600);

        AtomicInteger counter = new AtomicInteger();
        RefreshTokenGenerator refreshTokenGenerator = new RefreshTokenGenerator() {
            @Override
            public GeneratedRefreshToken generate() {
                String raw = "refresh-" + counter.incrementAndGet();
                return new GeneratedRefreshToken(raw, hash(raw), 86400);
            }

            @Override
            public String hash(String rawToken) {
                return "sha256:" + rawToken;
            }
        };

        SessionTokenIssuer sessionTokenIssuer = new SessionTokenIssuer(
                tokenIssuer, refreshTokenGenerator, refreshTokenRepository);

        authenticateHandler = new AuthenticateAccountHandler(accountRepository, passwordHasher, sessionTokenIssuer);
        currentAccountQuery = new GetCurrentAccountQuery(accountRepository);
    }

    private Account seed(String email, String password) {
        Account account = Account.register(email, "hash:" + password, "Usuario");
        return accountRepository.save(account);
    }

    @Test
    @DisplayName("El login válido devuelve token y datos públicos de la cuenta")
    void loginReturnsTokenAndAccount() {
        Account account = seed("usuario@ufps.edu.co", PASSWORD);

        LoginResult result = authenticateHandler.execute(new LoginCommand("Usuario@UFPS.EDU.CO ", PASSWORD));

        assertEquals("token-" + account.getAccountId().value(), result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(3600, result.expiresIn());
        assertEquals("refresh-1", result.refreshToken());
        assertEquals(86400, result.refreshExpiresIn());
        assertNotNull(refreshTokenRepository.findByTokenHash("sha256:refresh-1").orElse(null));
        assertEquals(account.getAccountId().value(), result.account().accountId());
        assertEquals("usuario@ufps.edu.co", result.account().email());
        assertEquals("ACTIVE", result.account().status().name());
    }

    @Test
    @DisplayName("El login con email inexistente produce 401")
    void loginWithUnknownEmailFails() {
        assertThrows(UnauthorizedException.class, () ->
                authenticateHandler.execute(new LoginCommand("nadie@ufps.edu.co", PASSWORD)));
    }

    @Test
    @DisplayName("El login con contraseña incorrecta produce 401")
    void loginWithWrongPasswordFails() {
        seed("usuario@ufps.edu.co", PASSWORD);

        assertThrows(UnauthorizedException.class, () ->
                authenticateHandler.execute(new LoginCommand("usuario@ufps.edu.co", "OtraClave123!")));
    }

    @Test
    @DisplayName("Una cuenta suspendida con credenciales válidas produce 403")
    void suspendedAccountCannotLogin() {
        Account account = seed("usuario@ufps.edu.co", PASSWORD);
        account.suspend();
        accountRepository.save(account);

        assertThrows(ForbiddenException.class, () ->
                authenticateHandler.execute(new LoginCommand("usuario@ufps.edu.co", PASSWORD)));
    }

    @Test
    @DisplayName("La consulta de la cuenta actual devuelve el perfil")
    void currentAccountReturnsProfile() {
        Account account = seed("usuario@ufps.edu.co", PASSWORD);

        AccountDto dto = currentAccountQuery.execute(account.getAccountId().value());

        assertEquals(account.getAccountId().value(), dto.accountId());
        assertEquals(account.getEmail(), dto.email());
        assertEquals(account.getRole(), dto.role());
        assertEquals(account.getStatus(), dto.status());
    }

    @Test
    @DisplayName("La consulta de una cuenta inexistente produce 404")
    void currentAccountNotFound() {
        assertThrows(NotFoundException.class, () ->
                currentAccountQuery.execute(AccountId.generate().value()));
    }
}
