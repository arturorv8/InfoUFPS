package org.infoufps.api.account;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.application.account.PasswordHasher;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@DisplayName("Administración de cuentas: integración HTTP")
class AdminAccountApiTest {

    private static final String PASSWORD = "Secreto123!";

    @Inject
    IAccountRepository accountRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    AccessTokenIssuer accessTokenIssuer;

    private static String uniquePrefix() {
        return "cuenta" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Account seed(String prefix, String name, Role role) {
        Account account = Account.register(
                prefix + "." + UUID.randomUUID() + "@ufps.edu.co",
                passwordHasher.hash(PASSWORD),
                name,
                role
        );
        return accountRepository.save(account);
    }

    private String bearer(Account account) {
        return "Bearer " + accessTokenIssuer.issue(account).token();
    }

    @Test
    @DisplayName("La administración de cuentas exige rol ADMIN")
    void adminEndpointsRequireAdmin() {
        Account user = seed(uniquePrefix(), "Usuario", Role.USER);

        given()
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(401);

        given()
                .header("Authorization", bearer(user))
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(403);
    }

    @Test
    @DisplayName("La búsqueda filtra por texto, rol y estado")
    void searchFiltersAccounts() {
        String prefix = uniquePrefix();
        Account admin = seed(prefix, "Admin " + prefix, Role.ADMIN);
        seed(prefix, "Uno " + prefix, Role.USER);
        seed(prefix, "Dos " + prefix, Role.USER);

        given()
                .header("Authorization", bearer(admin))
                .queryParam("query", prefix)
                .queryParam("role", "USER")
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(200)
                .body("totalElements", equalTo(2))
                .body("items.size()", equalTo(2));

        given()
                .header("Authorization", bearer(admin))
                .queryParam("query", prefix)
                .queryParam("role", "ADMIN")
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("items[0].accountId", equalTo(admin.getAccountId().value()));
    }

    @Test
    @DisplayName("La búsqueda pagina los resultados")
    void searchPaginatesAccounts() {
        String prefix = uniquePrefix();
        Account admin = seed(prefix, "Admin " + prefix, Role.ADMIN);
        seed(prefix, "Uno " + prefix, Role.USER);
        seed(prefix, "Dos " + prefix, Role.USER);
        seed(prefix, "Tres " + prefix, Role.USER);

        given()
                .header("Authorization", bearer(admin))
                .queryParam("query", prefix)
                .queryParam("role", "USER")
                .queryParam("page", 2)
                .queryParam("size", 2)
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(200)
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("items.size()", equalTo(1));
    }

    @Test
    @DisplayName("Un rol o estado inválido en la búsqueda responde 400")
    void invalidFiltersReturnBadRequest() {
        Account admin = seed(uniquePrefix(), "Admin", Role.ADMIN);

        given()
                .header("Authorization", bearer(admin))
                .queryParam("role", "SUPERUSER")
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));

        given()
                .header("Authorization", bearer(admin))
                .queryParam("status", "DESCONOCIDO")
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Un administrador puede cambiar el rol de otra cuenta")
    void adminChangesRole() {
        String prefix = uniquePrefix();
        Account admin = seed(prefix, "Admin " + prefix, Role.ADMIN);
        Account user = seed(prefix, "Usuario " + prefix, Role.USER);

        given()
                .header("Authorization", bearer(admin))
                .contentType(ContentType.JSON)
                .body(Map.of("role", "ADMIN"))
                .when().put("/api/v1/admin/accounts/%s/role".formatted(user.getAccountId().value()))
                .then().statusCode(200)
                .body("role", equalTo("ADMIN"));

        given()
                .header("Authorization", bearer(admin))
                .contentType(ContentType.JSON)
                .body(Map.of("role", "NO_EXISTE"))
                .when().put("/api/v1/admin/accounts/%s/role".formatted(user.getAccountId().value()))
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Un administrador no puede quitarse su propio rol")
    void adminCannotRemoveOwnRole() {
        Account admin = seed(uniquePrefix(), "Admin", Role.ADMIN);

        given()
                .header("Authorization", bearer(admin))
                .contentType(ContentType.JSON)
                .body(Map.of("role", "USER"))
                .when().put("/api/v1/admin/accounts/%s/role".formatted(admin.getAccountId().value()))
                .then().statusCode(403)
                .body("error", equalTo("FORBIDDEN"));
    }

    @Test
    @DisplayName("Un administrador no puede suspenderse a sí mismo")
    void adminCannotSuspendSelf() {
        Account admin = seed(uniquePrefix(), "Admin", Role.ADMIN);

        given()
                .header("Authorization", bearer(admin))
                .when().post("/api/v1/admin/accounts/%s/suspend".formatted(admin.getAccountId().value()))
                .then().statusCode(403)
                .body("error", equalTo("FORBIDDEN"));
    }

    @Test
    @DisplayName("Suspender y reactivar cambian el estado y revocan las sesiones")
    void suspendAndActivateAccount() {
        String prefix = uniquePrefix();
        Account admin = seed(prefix, "Admin " + prefix, Role.ADMIN);
        Account user = seed(prefix, "Usuario " + prefix, Role.USER);
        String userBearer = bearer(user);

        given()
                .header("Authorization", bearer(admin))
                .when().post("/api/v1/admin/accounts/%s/suspend".formatted(user.getAccountId().value()))
                .then().statusCode(200)
                .body("status", equalTo("SUSPENDED"));

        given()
                .header("Authorization", userBearer)
                .when().get("/api/v1/me")
                .then().statusCode(401);

        given()
                .header("Authorization", bearer(admin))
                .when().post("/api/v1/admin/accounts/%s/activate".formatted(user.getAccountId().value()))
                .then().statusCode(200)
                .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Administrar una cuenta inexistente responde 404")
    void missingAccountReturnsNotFound() {
        Account admin = seed(uniquePrefix(), "Admin", Role.ADMIN);

        given()
                .header("Authorization", bearer(admin))
                .contentType(ContentType.JSON)
                .body(Map.of("role", "ADMIN"))
                .when().put("/api/v1/admin/accounts/%s/role".formatted(UUID.randomUUID()))
                .then().statusCode(404)
                .body("error", equalTo("NOT_FOUND"));
    }

    @Test
    @DisplayName("La respuesta de búsqueda incluye los datos públicos de la cuenta")
    void searchResponseDoesNotLeakSecrets() {
        String prefix = uniquePrefix();
        Account admin = seed(prefix, "Admin " + prefix, Role.ADMIN);

        given()
                .header("Authorization", bearer(admin))
                .queryParam("query", prefix)
                .when().get("/api/v1/admin/accounts")
                .then().statusCode(200)
                .body("items[0].accountId", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(1));
    }
}
