package org.infoufps.api.calendar;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@DisplayName("API del calendario: integración HTTP")
class CalendarApiTest {

    private static final String ADMIN_ID = "admin-api-test";
    private static final String USER_ID = "user-api-test";

    @Inject
    IAccountRepository accountRepository;

    @Inject
    AccessTokenIssuer accessTokenIssuer;

    @BeforeEach
    void seedAccounts() {
        seedAccount(ADMIN_ID, "admin-api-test@ufps.edu.co", "Admin API Test", Role.ADMIN);
        seedAccount(USER_ID, "user-api-test@ufps.edu.co", "User API Test", Role.USER);
    }

    private String bearer(String accountId) {
        Account account = accountRepository.findById(AccountId.of(accountId)).orElseThrow();
        return "Bearer " + accessTokenIssuer.issue(account).token();
    }

    private void seedAccount(String id, String email, String name, Role role) {
        if (accountRepository.existsByEmail(email)) {
            return;
        }
        accountRepository.save(Account.of(
                AccountId.of(id),
                name,
                email,
                "hash",
                role,
                BogotaTime.now()
        ));
    }

    private static String uniqueTitle(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }

    private static Map<String, Object> timedEvent(String title, String date, String startTime) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("type", "ACADEMICO");
        body.put("location", "Bloque A");
        body.put("startDate", date);
        body.put("endDate", date);
        body.put("startTime", startTime);
        body.put("endTime", "10:00");
        body.put("description", "Descripción de prueba");
        body.put("infoUrl", "https://ufps.edu.co/evento");
        return body;
    }

    private String createPublishedEvent(String title, String date, String startTime) {
        return given()
                .header("Authorization", bearer(ADMIN_ID))
                .contentType(ContentType.JSON)
                .body(timedEvent(title, date, startTime))
                .when()
                .post("/api/v1/admin/events")
                .then()
                .statusCode(201)
                .body("status", is("PUBLISHED"))
                .body("eventId", notNullValue())
                .extract()
                .path("eventId");
    }

    @Test
    @DisplayName("El catálogo de tipos incluye los tipos restringidos")
    void typesCatalog() {
        given()
                .when().get("/api/v1/public/events/types")
                .then().statusCode(200)
                .body("value", hasItem("CALENDARIO_ACADEMICO"))
                .body("value", hasItem("INSTITUCIONAL"));
    }

    @Test
    @DisplayName("El resumen exige from y to")
    void summaryRequiresDates() {
        given()
                .when().get("/api/v1/public/events/summary")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Un tipo de evento inválido produce 400")
    void invalidTypeProducesBadRequest() {
        given()
                .queryParam("from", "2030-01-01")
                .queryParam("to", "2030-01-10")
                .queryParam("types", "NO_EXISTE")
                .when().get("/api/v1/public/events")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Un rango mayor a 62 días produce 400")
    void tooWideRangeProducesBadRequest() {
        given()
                .queryParam("from", "2030-01-01")
                .queryParam("to", "2030-04-01")
                .when().get("/api/v1/public/events")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));
    }

    @Test
    @DisplayName("El administrador crea un evento publicado y se exponen detalle, share e ICS")
    void adminCreatesPublishedEvent() {
        String title = uniqueTitle("Feria");
        String id = createPublishedEvent(title, "2030-01-15", "08:00");

        given()
                .when().get("/api/v1/public/events/" + id)
                .then().statusCode(200)
                .body("title", is(title))
                .body("type", is("ACADEMICO"))
                .body("allDay", is(false));

        given()
                .when().get("/api/v1/public/events/" + id + "/share")
                .then().statusCode(200)
                .body("eventUrl", containsString(id))
                .body("googleCalendarUrl", containsString("calendar.google.com"));

        given()
                .when().get("/api/v1/public/events/" + id + "/calendar.ics")
                .then().statusCode(200)
                .contentType(containsString("text/calendar"))
                .body(containsString("BEGIN:VCALENDAR"))
                .body(containsString("UID:event-" + id + "@infoufps"));

        given()
                .when().get("/share/events/" + id)
                .then().statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString(title));
    }

    @Test
    @DisplayName("Un evento inexistente produce 404")
    void unknownEventProducesNotFound() {
        given()
                .when().get("/api/v1/public/events/no-existe")
                .then().statusCode(404)
                .body("error", is("NOT_FOUND"));
    }

    @Test
    @DisplayName("Crear un evento duplicado publicado produce 409")
    void duplicatePublishedEventProducesConflict() {
        String title = uniqueTitle("Duplicado");
        createPublishedEvent(title, "2030-02-10", "08:00");

        given()
                .header("Authorization", bearer(ADMIN_ID))
                .contentType(ContentType.JSON)
                .body(timedEvent(title, "2030-02-10", "08:00"))
                .when().post("/api/v1/admin/events")
                .then().statusCode(409)
                .body("error", is("CONFLICT"));
    }

    @Test
    @DisplayName("Los endpoints de usuario exigen una cuenta autenticada")
    void userEndpointsRequireAccount() {
        given()
                .contentType(ContentType.JSON)
                .body(timedEvent(uniqueTitle("Sin cuenta"), "2030-03-10", "08:00"))
                .when().post("/api/v1/events")
                .then().statusCode(401)
                .body("error", is("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Un usuario propone un evento en revisión")
    void userProposesEvent() {
        String title = uniqueTitle("Propuesta");

        given()
                .header("Authorization", bearer(USER_ID))
                .contentType(ContentType.JSON)
                .body(timedEvent(title, "2030-03-10", "08:00"))
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .body("status", is("UNDER_REVIEW"))
                .body("proposedBy", is(USER_ID));

        given()
                .header("Authorization", bearer(ADMIN_ID))
                .queryParam("status", "UNDER_REVIEW")
                .queryParam("size", "100")
                .when().get("/api/v1/admin/events")
                .then().statusCode(200)
                .body("items.event.title", hasItem(title));
    }

    @Test
    @DisplayName("Un usuario no puede proponer tipos restringidos ni fechas pasadas")
    void userCannotProposeRestrictedTypeOrPastDate() {
        Map<String, Object> institutional = timedEvent(uniqueTitle("Institucional"), "2030-04-10", "08:00");
        institutional.put("type", "INSTITUCIONAL");

        given()
                .header("Authorization", bearer(USER_ID))
                .contentType(ContentType.JSON)
                .body(institutional)
                .when().post("/api/v1/events")
                .then().statusCode(400);

        given()
                .header("Authorization", bearer(USER_ID))
                .contentType(ContentType.JSON)
                .body(timedEvent(uniqueTitle("Pasado"), "2020-04-10", "08:00"))
                .when().post("/api/v1/events")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("Un usuario no puede acceder a los endpoints administrativos")
    void userCannotAccessAdminEndpoints() {
        given()
                .header("Authorization", bearer(USER_ID))
                .contentType(ContentType.JSON)
                .body(timedEvent(uniqueTitle("No autorizado"), "2030-05-10", "08:00"))
                .when().post("/api/v1/admin/events")
                .then().statusCode(403)
                .body("error", is("FORBIDDEN"));
    }

    @Test
    @DisplayName("El flujo de favoritos es idempotente y solo admite eventos publicados")
    void favoriteFlow() {
        String id = createPublishedEvent(uniqueTitle("Favorito"), "2030-06-10", "08:00");

        given()
                .header("Authorization", bearer(USER_ID))
                .when().put("/api/v1/events/" + id + "/favorite")
                .then().statusCode(204);

        given()
                .header("Authorization", bearer(USER_ID))
                .when().put("/api/v1/events/" + id + "/favorite")
                .then().statusCode(204);

        given()
                .header("Authorization", bearer(USER_ID))
                .when().get("/api/v1/events/favorites/ids")
                .then().statusCode(200)
                .body("$", hasItem(id));

        given()
                .header("Authorization", bearer(USER_ID))
                .when().delete("/api/v1/events/" + id + "/favorite")
                .then().statusCode(204);

        given()
                .header("Authorization", bearer(USER_ID))
                .when().get("/api/v1/events/favorites/ids")
                .then().statusCode(200)
                .body("$", org.hamcrest.Matchers.not(hasItem(id)));
    }
}
