package org.infoufps.api.account;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.application.account.PasswordHasher;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.Content;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.content.InteractionStats;
import org.infoufps.domain.content.ModuleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@DisplayName("Autenticación, perfil y comentarios: integración HTTP")
class AuthApiTest {

    private static final String PASSWORD = "Secreto123!";

    @Inject
    IAccountRepository accountRepository;

    @Inject
    IContentRepository contentRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    AccessTokenIssuer accessTokenIssuer;

    private static String uniqueEmail() {
        return "user." + UUID.randomUUID() + "@ufps.edu.co";
    }

    private Account seedAccount() {
        Account account = Account.register(uniqueEmail(), passwordHasher.hash(PASSWORD), "Usuario de prueba");
        return accountRepository.save(account);
    }

    private String bearer(Account account) {
        return "Bearer " + accessTokenIssuer.issue(account).token();
    }

    private Content seedPublishedContent() {
        Content content = Content.create(
                "Contenido " + UUID.randomUUID(),
                "Subtítulo",
                "Descripción",
                null,
                "ACADEMICO",
                Set.of(),
                "https://info.ufps.edu.co",
                InteractionStats.empty(),
                Status.PUBLISHED,
                ModuleType.LA_VITRINA
        );
        contentRepository.save(content);
        return content;
    }

    @Test
    @DisplayName("El registro crea una cuenta activa con rol USER y sin exponer la contraseña")
    void registerCreatesActiveUser() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", PASSWORD, "name", "Nuevo Usuario"))
                .when().post("/api/v1/auth/register")
                .then().statusCode(201)
                .body("accountId", notNullValue())
                .body("email", equalTo(email))
                .body("name", equalTo("Nuevo Usuario"))
                .body("role", equalTo("USER"))
                .body("status", equalTo("ACTIVE"))
                .body("$", not(hasKey("password")))
                .body("$", not(hasKey("passwordHash")));
    }

    @Test
    @DisplayName("El registro normaliza el email a minúsculas")
    void registerNormalizesEmail() {
        String email = "Usuario." + UUID.randomUUID() + "@UFPS.EDU.CO";

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", PASSWORD, "name", "Mayúsculas"))
                .when().post("/api/v1/auth/register")
                .then().statusCode(201)
                .body("email", equalTo(email.toLowerCase()));
    }

    @Test
    @DisplayName("El registro de un email existente responde 409")
    void registerDuplicateEmailReturnsConflict() {
        Account existing = seedAccount();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", existing.getEmail().toUpperCase(), "password", PASSWORD, "name", "Duplicado"))
                .when().post("/api/v1/auth/register")
                .then().statusCode(409)
                .body("error", equalTo("CONFLICT"));
    }

    @Test
    @DisplayName("El registro valida la contraseña y responde 400 si es corta")
    void registerRejectsShortPassword() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", uniqueEmail(), "password", "corta", "name", "Corta"))
                .when().post("/api/v1/auth/register")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("El login devuelve un token Bearer y los datos públicos de la cuenta")
    void loginReturnsToken() {
        Account account = seedAccount();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", equalTo(3600))
                .body("refreshToken", notNullValue())
                .body("refreshExpiresIn", equalTo(2592000))
                .body("account.accountId", equalTo(account.getAccountId().value()))
                .body("account.email", equalTo(account.getEmail()))
                .body("account.status", equalTo("ACTIVE"))
                .body("$", not(hasKey("password")))
                .body("account", not(hasKey("passwordHash")));
    }

    @Test
    @DisplayName("El login con contraseña incorrecta responde 401")
    void loginWithWrongPasswordReturnsUnauthorized() {
        Account account = seedAccount();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", "OtraClave123!"))
                .when().post("/api/v1/auth/login")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("El login con email inexistente responde 401")
    void loginWithUnknownEmailReturnsUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", uniqueEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Una cuenta suspendida no puede iniciar sesión")
    void suspendedAccountCannotLogin() {
        Account account = seedAccount();
        account.suspend();
        accountRepository.save(account);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(403)
                .body("error", equalTo("FORBIDDEN"));
    }

    @Test
    @DisplayName("GET /me exige token y devuelve la cuenta autenticada")
    void meRequiresTokenAndReturnsAccount() {
        Account account = seedAccount();

        given()
                .header("Authorization", bearer(account))
                .when().get("/api/v1/me")
                .then().statusCode(200)
                .body("accountId", equalTo(account.getAccountId().value()))
                .body("email", equalTo(account.getEmail()))
                .body("role", equalTo("USER"))
                .body("status", equalTo("ACTIVE"));

        given()
                .when().get("/api/v1/me")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));

        given()
                .header("Authorization", "Bearer token-invalido")
                .when().get("/api/v1/me")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Un token emitido deja de servir si la cuenta es suspendida")
    void suspendedAccountTokenIsRejected() {
        Account account = seedAccount();
        String token = bearer(account);

        account.suspend();
        accountRepository.save(account);

        given()
                .header("Authorization", token)
                .when().get("/api/v1/me")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("El comentario toma el autor del token y no del cliente")
    void commentAuthorComesFromToken() {
        Account account = seedAccount();
        Content content = seedPublishedContent();

        given()
                .header("Authorization", bearer(account))
                .contentType(ContentType.JSON)
                .body(Map.of("text", "Buen aporte", "authorId", AccountId.generate().value()))
                .when().post("/api/v1/contents/%s/comments".formatted(content.getId().value()))
                .then().statusCode(201)
                .body("authorId", equalTo(account.getAccountId().value()))
                .body("text", equalTo("Buen aporte"))
                .body("contentId", equalTo(content.getId().value()));
    }

    @Test
    @DisplayName("Crear un comentario sin token responde 401")
    void commentWithoutTokenReturnsUnauthorized() {
        Content content = seedPublishedContent();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("text", "Sin token"))
                .when().post("/api/v1/contents/%s/comments".formatted(content.getId().value()))
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("El listado de comentarios es público")
    void commentsListIsPublic() {
        Content content = seedPublishedContent();

        given()
                .when().get("/api/v1/contents/%s/comments".formatted(content.getId().value()))
                .then().statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @DisplayName("La renovación emite un token nuevo y el anterior no se puede reutilizar")
    void refreshRotatesTokenAndRejectsReuse() {
        Account account = seedAccount();

        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("refreshToken");

        String rotated = given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", refreshToken))
                .when().post("/api/v1/auth/refresh")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", not(equalTo(refreshToken)))
                .extract().path("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", refreshToken))
                .when().post("/api/v1/auth/refresh")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));

        // La reutilización revoca todos los tokens, incluido el recién rotado.
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", rotated))
                .when().post("/api/v1/auth/refresh")
                .then().statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Cerrar sesión revoca el token de renovación")
    void logoutRevokesRefreshToken() {
        Account account = seedAccount();

        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", refreshToken))
                .when().post("/api/v1/auth/logout")
                .then().statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", refreshToken))
                .when().post("/api/v1/auth/refresh")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("PATCH /me actualiza el nombre de la cuenta autenticada")
    void updateNameRequiresTokenAndChangesName() {
        Account account = seedAccount();

        given()
                .header("Authorization", bearer(account))
                .contentType(ContentType.JSON)
                .body(Map.of("name", "  Nombre Nuevo  "))
                .when().patch("/api/v1/me")
                .then().statusCode(200)
                .body("name", equalTo("Nombre Nuevo"))
                .body("accountId", equalTo(account.getAccountId().value()));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Sin token"))
                .when().patch("/api/v1/me")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Cambiar contraseña exige la actual y revoca las sesiones")
    void changePasswordRequiresCurrentAndRevokesSessions() {
        Account account = seedAccount();
        String bearer = bearer(account);

        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("refreshToken");

        given()
                .header("Authorization", bearer)
                .contentType(ContentType.JSON)
                .body(Map.of("currentPassword", "Incorrecta123!", "newPassword", "NuevaClave123!"))
                .when().put("/api/v1/me/password")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));

        given()
                .header("Authorization", bearer)
                .contentType(ContentType.JSON)
                .body(Map.of("currentPassword", PASSWORD, "newPassword", "NuevaClave123!"))
                .when().put("/api/v1/me/password")
                .then().statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", refreshToken))
                .when().post("/api/v1/auth/refresh")
                .then().statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", PASSWORD))
                .when().post("/api/v1/auth/login")
                .then().statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", account.getEmail(), "password", "NuevaClave123!"))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200);
    }
}
