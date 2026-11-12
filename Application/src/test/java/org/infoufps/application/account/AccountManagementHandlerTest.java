package org.infoufps.application.account;

import org.infoufps.application.ApplicationUseCasesTest;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountSearchCriteria;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gestión de sesiones y administración de cuentas")
class AccountManagementHandlerTest {

    private static final String PASSWORD = "Secreto123!";

    private ApplicationUseCasesTest.InMemoryAccountRepository accountRepository;
    private ApplicationUseCasesTest.InMemoryRefreshTokenRepository refreshTokenRepository;
    private PasswordHasher passwordHasher;
    private AuthenticateAccountHandler authenticateHandler;
    private RefreshSessionHandler refreshSessionHandler;
    private LogoutHandler logoutHandler;
    private UpdateAccountNameHandler updateNameHandler;
    private ChangePasswordHandler changePasswordHandler;
    private ChangeAccountRoleHandler changeRoleHandler;
    private SuspendAccountHandler suspendHandler;
    private ActivateAccountHandler activateHandler;
    private SearchAccountsQuery searchQuery;

    @BeforeEach
    void setUp() {
        accountRepository = new ApplicationUseCasesTest.InMemoryAccountRepository();
        refreshTokenRepository = new ApplicationUseCasesTest.InMemoryRefreshTokenRepository();

        passwordHasher = new PasswordHasher() {
            @Override
            public String hash(String password) {
                return "hash:" + password;
            }

            @Override
            public boolean verify(String password, String hash) {
                return hash != null && hash.equals("hash:" + password);
            }
        };

        AtomicInteger accessCounter = new AtomicInteger();
        AccessTokenIssuer accessTokenIssuer = account ->
                new IssuedAccessToken("token-" + account.getAccountId().value()
                        + "-" + accessCounter.incrementAndGet(), 3600);

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
                accessTokenIssuer, refreshTokenGenerator, refreshTokenRepository);
        RefreshTokenRevocationService revocationService =
                new RefreshTokenRevocationService(refreshTokenRepository);
        AccountAdminGuard adminGuard = new AccountAdminGuard(accountRepository);

        authenticateHandler = new AuthenticateAccountHandler(
                accountRepository, passwordHasher, sessionTokenIssuer);
        refreshSessionHandler = new RefreshSessionHandler(
                refreshTokenRepository, accountRepository, refreshTokenGenerator,
                sessionTokenIssuer, revocationService);
        logoutHandler = new LogoutHandler(refreshTokenRepository, refreshTokenGenerator);
        updateNameHandler = new UpdateAccountNameHandler(accountRepository);
        changePasswordHandler = new ChangePasswordHandler(
                accountRepository, refreshTokenRepository, passwordHasher);
        changeRoleHandler = new ChangeAccountRoleHandler(accountRepository, adminGuard);
        suspendHandler = new SuspendAccountHandler(
                accountRepository, refreshTokenRepository, adminGuard);
        activateHandler = new ActivateAccountHandler(accountRepository);
        searchQuery = new SearchAccountsQuery(accountRepository);
    }

    private Account seed(String email, String name, Role role) {
        Account account = Account.register(email, passwordHasher.hash(PASSWORD), name, role);
        return accountRepository.save(account);
    }

    private LoginResult login(Account account) {
        return authenticateHandler.execute(new LoginCommand(account.getEmail(), PASSWORD));
    }

    @Test
    @DisplayName("La renovación rota el token: el anterior queda revocado y se emite uno nuevo")
    void refreshRotatesToken() {
        Account account = seed("user@ufps.edu.co", "Usuario", Role.USER);
        LoginResult first = login(account);

        LoginResult second = refreshSessionHandler.execute(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());
        assertNotEquals(first.accessToken(), second.accessToken());
        assertTrue(refreshTokenRepository.findByTokenHash("sha256:" + first.refreshToken())
                .orElseThrow().isRevoked());
        assertFalse(refreshTokenRepository.findByTokenHash("sha256:" + second.refreshToken())
                .orElseThrow().isRevoked());
    }

    @Test
    @DisplayName("Reutilizar un token ya rotado revoca todos los tokens de la cuenta")
    void reusingRotatedTokenRevokesAllTokens() {
        Account account = seed("user@ufps.edu.co", "Usuario", Role.USER);
        LoginResult first = login(account);
        LoginResult second = refreshSessionHandler.execute(first.refreshToken());

        assertThrows(UnauthorizedException.class,
                () -> refreshSessionHandler.execute(first.refreshToken()));

        assertTrue(refreshTokenRepository.tokensOf(account.getAccountId()).stream()
                .allMatch(token -> token.isRevoked()),
                "Todos los tokens de la cuenta deben quedar revocados");
        assertTrue(refreshTokenRepository.findByTokenHash("sha256:" + second.refreshToken())
                .orElseThrow().isRevoked());
    }

    @Test
    @DisplayName("Un token de renovación desconocido produce 401")
    void unknownRefreshTokenIsRejected() {
        assertThrows(UnauthorizedException.class,
                () -> refreshSessionHandler.execute("no-existe"));
    }

    @Test
    @DisplayName("Cerrar sesión revoca el token presentado y es idempotente")
    void logoutRevokesTokenIdempotently() {
        Account account = seed("user@ufps.edu.co", "Usuario", Role.USER);
        LoginResult session = login(account);

        logoutHandler.execute(session.refreshToken());
        logoutHandler.execute(session.refreshToken());

        assertTrue(refreshTokenRepository.findByTokenHash("sha256:" + session.refreshToken())
                .orElseThrow().isRevoked());
    }

    @Test
    @DisplayName("Editar el nombre actualiza el perfil")
    void updateNameChangesProfile() {
        Account account = seed("user@ufps.edu.co", "Usuario", Role.USER);

        AccountDto dto = updateNameHandler.execute(account.getAccountId().value(), "  Nuevo Nombre  ");

        assertEquals("Nuevo Nombre", dto.name());
        assertEquals("Nuevo Nombre", accountRepository.findById(account.getAccountId())
                .orElseThrow().getName());
    }

    @Test
    @DisplayName("Cambiar contraseña exige la actual y revoca todas las sesiones")
    void changePasswordRequiresCurrentAndRevokesSessions() {
        Account account = seed("user@ufps.edu.co", "Usuario", Role.USER);
        LoginResult session = login(account);

        assertThrows(DomainException.class, () -> changePasswordHandler.execute(
                account.getAccountId().value(),
                new ChangePasswordCommand("Incorrecta123!", "NuevaClave123!")));

        changePasswordHandler.execute(account.getAccountId().value(),
                new ChangePasswordCommand(PASSWORD, "NuevaClave123!"));

        Account updated = accountRepository.findById(account.getAccountId()).orElseThrow();
        assertEquals("hash:NuevaClave123!", updated.getPasswordHash());
        assertTrue(refreshTokenRepository.findByTokenHash("sha256:" + session.refreshToken())
                .orElseThrow().isRevoked());
    }

    @Test
    @DisplayName("Un administrador no puede quitarse su propio rol")
    void adminCannotRemoveOwnRole() {
        Account admin = seed("admin@ufps.edu.co", "Admin", Role.ADMIN);

        assertThrows(ForbiddenException.class, () -> changeRoleHandler.execute(
                admin.getAccountId().value(), Role.USER, admin.getAccountId().value()));
    }

    @Test
    @DisplayName("No se puede dejar el sistema sin administradores activos")
    void lastActiveAdminCannotBeDemotedOrSuspended() {
        Account admin = seed("admin@ufps.edu.co", "Admin", Role.ADMIN);

        assertThrows(ConflictException.class, () -> changeRoleHandler.execute(
                admin.getAccountId().value(), Role.USER, "otro-admin"));

        assertThrows(ConflictException.class, () -> suspendHandler.execute(
                admin.getAccountId().value(), "otro-admin"));
    }

    @Test
    @DisplayName("Un administrador no puede suspenderse a sí mismo")
    void adminCannotSuspendSelf() {
        Account admin = seed("admin@ufps.edu.co", "Admin", Role.ADMIN);

        assertThrows(ForbiddenException.class, () -> suspendHandler.execute(
                admin.getAccountId().value(), admin.getAccountId().value()));
    }

    @Test
    @DisplayName("Cambiar rol, suspender y reactivar surten efecto")
    void adminOperationsChangeAccountState() {
        Account admin = seed("admin@ufps.edu.co", "Admin", Role.ADMIN);
        Account user = seed("user@ufps.edu.co", "Usuario", Role.USER);
        String acting = admin.getAccountId().value();

        AccountDto promoted = changeRoleHandler.execute(user.getAccountId().value(), Role.ADMIN, acting);
        assertEquals(Role.ADMIN, promoted.role());

        AccountDto suspended = suspendHandler.execute(user.getAccountId().value(), acting);
        assertEquals(AccountStatus.SUSPENDED, suspended.status());

        AccountDto activated = activateHandler.execute(user.getAccountId().value());
        assertEquals(AccountStatus.ACTIVE, activated.status());
    }

    @Test
    @DisplayName("Suspender revoca las sesiones de la cuenta")
    void suspensionRevokesSessions() {
        Account admin = seed("admin@ufps.edu.co", "Admin", Role.ADMIN);
        Account user = seed("user@ufps.edu.co", "Usuario", Role.USER);
        LoginResult session = login(user);

        suspendHandler.execute(user.getAccountId().value(), admin.getAccountId().value());

        assertTrue(refreshTokenRepository.findByTokenHash("sha256:" + session.refreshToken())
                .orElseThrow().isRevoked());
    }

    @Test
    @DisplayName("La búsqueda filtra por texto, rol y estado y pagina los resultados")
    void searchFiltersAndPaginates() {
        seed("ana@ufps.edu.co", "Ana", Role.USER);
        seed("beto@ufps.edu.co", "Beto", Role.USER);
        Account carla = seed("carla@ufps.edu.co", "Carla", Role.ADMIN);
        carla.suspend();
        accountRepository.save(carla);

        PageResult<AccountDto> users = searchQuery.execute(
                new AccountSearchCriteria("ufps", Role.USER, null),
                PageRequest.of(1, 10));
        assertEquals(2, users.totalElements());

        PageResult<AccountDto> suspended = searchQuery.execute(
                new AccountSearchCriteria(null, null, AccountStatus.SUSPENDED),
                PageRequest.of(1, 10));
        assertEquals(1, suspended.totalElements());
        assertEquals("Carla", suspended.items().get(0).name());

        PageResult<AccountDto> firstPage = searchQuery.execute(
                new AccountSearchCriteria(null, null, null),
                PageRequest.of(1, 2));
        assertEquals(3, firstPage.totalElements());
        assertEquals(2, firstPage.items().size());
        assertEquals(2, firstPage.totalPages());
    }
}
