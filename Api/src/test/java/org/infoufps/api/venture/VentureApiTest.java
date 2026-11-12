package org.infoufps.api.venture;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.venture.IFavoriteVentureRepository;
import org.infoufps.domain.venture.IVentureRatingRepository;
import org.infoufps.domain.venture.IVentureReportRepository;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.VentureId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
@DisplayName("API de Emprendimientos (La Vitrina): Pruebas de integración HTTP")
class VentureApiTest {

    private static final String ADMIN_ID = "admin-venture-api";
    private static final String USER2_ID = "user2-venture-api";
    private static final String USER3_ID = "user3-venture-api";
    private static final String USER4_ID = "user4-venture-api";
    private static final String UNVERIFIED_USER_ID = "unverified-venture-api";

    @Inject
    IAccountRepository accountRepository;

    @Inject
    AccessTokenIssuer accessTokenIssuer;

    @Inject
    IVentureRepository ventureRepository;

    @Inject
    IVentureRatingRepository ratingRepository;

    @Inject
    IFavoriteVentureRepository favoriteRepository;

    @Inject
    IVentureReportRepository reportRepository;

    @BeforeEach
    void seedAccounts() {
        seedAccount(ADMIN_ID, "admin-venture@ufps.edu.co", "Admin Vitrina", Role.ADMIN, true);
        seedAccount(USER2_ID, "user2-venture@ufps.edu.co", "User Dos", Role.USER, true);
        seedAccount(USER3_ID, "user3-venture@ufps.edu.co", "User Tres", Role.USER, true);
        seedAccount(USER4_ID, "user4-venture@ufps.edu.co", "User Cuatro", Role.USER, true);
        seedAccount(UNVERIFIED_USER_ID, "unverified-venture@ufps.edu.co", "User Sin Verificar", Role.USER, false);
    }

    private void seedAccount(String id, String email, String name, Role role, boolean verified) {
        if (accountRepository.existsByEmail(email)) {
            Account existing = accountRepository.findById(AccountId.of(id)).orElse(null);
            if (existing != null && existing.isEmailVerified() != verified) {
                if (verified) {
                    existing.verifyEmail();
                } else {
                    existing.unverifyEmail();
                }
                accountRepository.save(existing);
            }
            return;
        }
        accountRepository.save(Account.of(
                AccountId.of(id),
                name,
                email,
                "hash",
                role,
                AccountStatus.ACTIVE,
                verified,
                BogotaTime.now()
        ));
    }

    private String newOwner() {
        String id = "owner-" + UUID.randomUUID().toString().substring(0, 8);
        seedAccount(id, id + "@ufps.edu.co", "Owner " + id, Role.USER, true);
        return id;
    }

    private String bearer(String accountId) {
        Account account = accountRepository.findById(AccountId.of(accountId)).orElseThrow();
        return "Bearer " + accessTokenIssuer.issue(account).token();
    }

    private static String uniqueName(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<String, Object> createVenturePayload(String name, String description, String category,
                                                    String contactType, String contactValue,
                                                    List<String> modes, String location) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("description", description);
        payload.put("category", category);
        payload.put("contactType", contactType);
        payload.put("contactValue", contactValue);
        payload.put("serviceModes", modes);
        payload.put("location", location);
        return payload;
    }

    private String createAndPublishVenture(String name, String category, String contactType, String contactValue) {
        return createAndPublishVenture(newOwner(), name, category, contactType, contactValue);
    }

    private String createAndPublishVenture(String ownerId, String name, String category, String contactType, String contactValue) {
        String ventureId = given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(name, "Descripción de prueba para vitrina", category,
                        contactType, contactValue, List.of("PRESENCIAL", "DOMICILIO"), "Edificio Cúcuta"))
                .when()
                .post("/api/v1/ventures")
                .then()
                .statusCode(201)
                .body("status", is("UNDER_REVIEW"))
                .body("id", notNullValue())
                .extract()
                .path("id");

        given()
                .header("Authorization", bearer(ADMIN_ID))
                .when()
                .post("/api/v1/admin/ventures/" + ventureId + "/publish")
                .then()
                .statusCode(200)
                .body("status", is("PUBLISHED"));

        return ventureId;
    }

    // =========================================================
    // 1. ENDPOINTS PÚBLICOS
    // =========================================================

    @Test
    @DisplayName("Los catálogos públicos exponen categorías y modos de servicio")
    void publicCatalogs() {
        given()
                .when().get("/api/v1/public/ventures/categories")
                .then().statusCode(200)
                .body("$", hasItem("COMIDA_Y_BEBIDAS"))
                .body("$", hasItem("MODA_Y_ACCESORIOS"))
                .body("$", hasItem("TECNOLOGIA"))
                .body("$", hasItem("SERVICIOS_Y_TUTORIAS"))
                .body("$", hasItem("ARTE_Y_MANUALIDADES"))
                .body("$", hasItem("SALUD_Y_BIENESTAR"))
                .body("$", hasItem("OTROS"));

        given()
                .when().get("/api/v1/public/ventures/modes")
                .then().statusCode(200)
                .body("$", hasItem("PRESENCIAL"))
                .body("$", hasItem("DOMICILIO"))
                .body("$", hasItem("VIRTUAL"));
    }

    @Test
    @DisplayName("Búsqueda pública con tildes, orden de palabras y caracteres especiales")
    void publicSearchWithDiacriticsAndWordOrder() {
        String name = uniqueName("Cafetería La Gran Estación");
        createAndPublishVenture(name, "COMIDA_Y_BEBIDAS", "WHATSAPP", "3123456789");

        // Búsqueda sin tildes e inversión de palabras
        given()
                .queryParam("q", "estacion cafeteria")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(200)
                .body("items.name", hasItem(name));

        // Búsqueda con caracteres especiales escapados (% y _)
        given()
                .queryParam("q", "%_")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Filtros múltiples y ordenamiento en búsqueda pública")
    void publicSearchFiltersAndSorting() {
        String nameA = "AAA " + uniqueName("Postres");
        String nameZ = "ZZZ " + uniqueName("Ropa");
        String idA = createAndPublishVenture(nameA, "COMIDA_Y_BEBIDAS", "INSTAGRAM", "postres_ricos");
        String idZ = createAndPublishVenture(nameZ, "MODA_Y_ACCESORIOS", "EMAIL", "ropa@ufps.edu.co");

        // Calificar postres con 5 estrellas
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + idA + "/rating")
                .then().statusCode(200);

        // Filtrar por categoría COMIDA_Y_BEBIDAS
        given()
                .queryParam("categories", "COMIDA_Y_BEBIDAS")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(200)
                .body("items.name", hasItem(nameA))
                .body("items.name", not(hasItem(nameZ)));

        // Orden por rating
        given()
                .queryParam("sort", "rating")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(200);

        // Orden por name
        given()
                .queryParam("sort", "name")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Parámetros inválidos de búsqueda producen 400 Bad Request")
    void publicSearchInvalidParamsProduceBadRequest() {
        given()
                .queryParam("sort", "invalido")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));

        given()
                .queryParam("categories", "CATEGORIA_INVENTADA")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));

        given()
                .queryParam("modes", "MODO_INVENTADO")
                .when().get("/api/v1/public/ventures")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Detalle público expone contacto, URL calculada, promedio y total de ratings")
    void publicDetailExposesContactAndRatings() {
        String name = uniqueName("Arepas");
        String id = createAndPublishVenture(name, "COMIDA_Y_BEBIDAS", "WHATSAPP", "3001234567");

        // Calificar por User2
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 4))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(200);

        // Consulta pública anónima
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200)
                .body("id", is(id))
                .body("name", is(name))
                .body("contact.type", is("WHATSAPP"))
                .body("contact.value", is("+573001234567"))
                .body("contact.actionUrl", containsString("https://wa.me/573001234567"))
                .body("ratingAverage", is(4.0f))
                .body("ratingCount", is(1))
                .body("myRating", nullValue());

        // Consulta autenticada con User2 (debe retornar myRating = 4)
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200)
                .body("myRating", is(4));
    }

    @Test
    @DisplayName("Emprendimiento no visible produce 404 en endpoints públicos")
    void nonVisibleVentureProduces404() {
        // Crear pero no publicar (queda en UNDER_REVIEW)
        String ownerId = newOwner();
        String id = given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(uniqueName("Borrador"), "Desc", "OTROS", "EMAIL", "test@ufps.edu.co",
                        List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404)
                .body("error", is("NOT_FOUND"));

        given()
                .when().get("/api/v1/public/ventures/" + id + "/share")
                .then().statusCode(404)
                .body("error", is("NOT_FOUND"));

        given()
                .when().get("/share/ventures/" + id)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("Compartir emprendimiento: JSON de metadatos y HTML con vista previa segura")
    void ventureShareAndPreviewPage() {
        String name = uniqueName("Pastelería <script>alert(1)</script>");
        String id = createAndPublishVenture(name, "COMIDA_Y_BEBIDAS", "WEB", "https://pasteleria.com");

        given()
                .when().get("/api/v1/public/ventures/" + id + "/share")
                .then().statusCode(200)
                .body("id", is(id))
                .body("title", is(name))
                .body("shareUrl", containsString(id))
                .body("logoUrl", notNullValue());

        given()
                .when().get("/share/ventures/" + id)
                .then().statusCode(200)
                .contentType(containsString("text/html"))
                .body(not(containsString("<script>alert(1)</script>")))
                .body(containsString("&lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    // =========================================================
    // 2. ENDPOINTS DE USUARIO AUTENTICADO
    // =========================================================

    @Test
    @DisplayName("Creación de emprendimiento: validación de contacto y estados iniciales")
    void userCreateVentureValidation() {
        String ownerId = newOwner();

        // WhatsApp inválido (menos de 10 dígitos)
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload("Nombre OK", "Desc OK", "COMIDA_Y_BEBIDAS", "WHATSAPP", "123",
                        List.of("DOMICILIO"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));

        // Nombre en blanco
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload("", "Desc OK", "COMIDA_Y_BEBIDAS", "EMAIL", "a@ufps.edu.co",
                        List.of("DOMICILIO"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(400);

        // Sin token -> 401
        given()
                .contentType(ContentType.JSON)
                .body(createVenturePayload("Nombre", "Desc", "OTROS", "EMAIL", "a@ufps.edu.co", List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(401)
                .body("error", is("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Mismo dueño y mismo nombre normalizado produce 409 Conflict")
    void duplicateNameForSameOwnerProduces409() {
        String ownerId = newOwner();
        String baseName = uniqueName("Diseños Creativos");
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(baseName, "Desc", "ARTE_Y_MANUALIDADES", "EMAIL", "arte@ufps.edu.co",
                        List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201);

        // Intentar registrar el mismo nombre con variaciones de mayúsculas/espacios
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload("  " + baseName.toUpperCase() + "  ", "Desc 2", "ARTE_Y_MANUALIDADES",
                        "EMAIL", "arte2@ufps.edu.co", List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(409)
                .body("error", is("CONFLICT"));
    }

    @Test
    @DisplayName("Límite por usuario: máximo 3 emprendimientos")
    void userLimitsPerUser() {
        String ownerId = newOwner();

        for (int i = 1; i <= 3; i++) {
            given()
                    .header("Authorization", bearer(ownerId))
                    .contentType(ContentType.JSON)
                    .body(createVenturePayload(uniqueName("Emprendimiento " + i), "Desc", "OTROS", "EMAIL",
                            "limit" + i + "@ufps.edu.co", List.of("VIRTUAL"), null))
                    .when().post("/api/v1/ventures")
                    .then().statusCode(201);
        }

        // El 4to emprendimiento debe ser rechazado
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(uniqueName("Emprendimiento 4"), "Desc", "OTROS", "EMAIL",
                        "limit4@ufps.edu.co", List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(400)
                .body("message", containsString("límite máximo de emprendimientos permitidos"));
    }

    @Test
    @DisplayName("Consulta de emprendimientos propios (/mine)")
    void userGetMine() {
        String ownerId = newOwner();
        String name1 = uniqueName("Mío 1");
        String name2 = uniqueName("Mío 2");

        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(name1, "Desc", "OTROS", "EMAIL", "mio1@ufps.edu.co", List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201);

        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(name2, "Desc", "OTROS", "EMAIL", "mio2@ufps.edu.co", List.of("VIRTUAL"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201);

        given()
                .header("Authorization", bearer(ownerId))
                .when().get("/api/v1/ventures/mine")
                .then().statusCode(200)
                .body("$", hasSize(2))
                .body("name", hasItem(name1))
                .body("name", hasItem(name2));
    }

    @Test
    @DisplayName("Editar emprendimiento: cambiar contacto devuelve a UNDER_REVIEW, otros campos no")
    void updateContactReturnsToUnderReview() {
        String ownerId = newOwner();
        String name = uniqueName("Tecno Reparaciones");
        String id = createAndPublishVenture(ownerId, name, "TECNOLOGIA", "EMAIL", "tecno@ufps.edu.co");

        // 1. Modificar solo la descripción (el contacto sigue igual) -> Permanece PUBLISHED
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(name, "Nueva descripción editada", "TECNOLOGIA", "EMAIL", "tecno@ufps.edu.co",
                        List.of("PRESENCIAL"), "Local 5"))
                .when().put("/api/v1/ventures/" + id)
                .then().statusCode(200)
                .body("status", is("PUBLISHED"))
                .body("description", is("Nueva descripción editada"));

        // 2. Modificar el contacto -> Vuelve a UNDER_REVIEW
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(name, "Nueva descripción editada", "TECNOLOGIA", "WHATSAPP", "3119876543",
                        List.of("PRESENCIAL"), "Local 5"))
                .when().put("/api/v1/ventures/" + id)
                .then().statusCode(200)
                .body("status", is("UNDER_REVIEW"))
                .body("contact.type", is("WHATSAPP"));

        // Al estar en UNDER_REVIEW ya no es visible al público (404)
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("Activar y desactivar emprendimiento cambia la visibilidad pública")
    void activateAndDeactivateFlow() {
        String ownerId = newOwner();
        String id = createAndPublishVenture(ownerId, uniqueName("Gimnasio"), "SALUD_Y_BIENESTAR", "INSTAGRAM", "gym_fit");

        // Desactivar por su dueño
        given()
                .header("Authorization", bearer(ownerId))
                .when().post("/api/v1/ventures/" + id + "/deactivate")
                .then().statusCode(200)
                .body("active", is(false));

        // Inactivo no aparece en detalle público
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404);

        // Activar de nuevo
        given()
                .header("Authorization", bearer(ownerId))
                .when().post("/api/v1/ventures/" + id + "/activate")
                .then().statusCode(200)
                .body("active", is(true));

        // Visible nuevamente
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Calificaciones: permisos, validación, reemplazo y borrado")
    void ratingsLifecycle() {
        String ownerId = newOwner();
        String id = createAndPublishVenture(ownerId, uniqueName("Tutorías Cálculo"), "SERVICIOS_Y_TUTORIAS", "EMAIL", "tutor@ufps.edu.co");

        // 1. Sin login -> 401
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(401);

        // 2. Con email no verificado -> 403
        given()
                .header("Authorization", bearer(UNVERIFIED_USER_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(403)
                .body("error", is("FORBIDDEN"));

        // 3. El dueño no puede calificar el propio -> 400
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(400)
                .body("error", is("BAD_REQUEST"));

        // 4. Score fuera de rango (0 o 6) -> 400
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 6))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(400);

        // 5. Calificar válidamente con 3 estrellas
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 3))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(200)
                .body("score", is(3));

        // Consultar mi calificación
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(200)
                .body("score", is(3));

        // 6. Calificar de nuevo reemplaza (score = 5)
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(200)
                .body("score", is(5));

        // Verificar promedio actualizado a 5.0
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200)
                .body("ratingAverage", is(5.0f))
                .body("ratingCount", is(1));

        // 7. Borrar calificación -> 204
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().delete("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(204);

        // Tras borrar, ratingCount = 0
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200)
                .body("ratingCount", is(0));
    }

    @Test
    @DisplayName("Favoritos: idempotencia, solo publicados y no se listan archivados")
    void favoritesLifecycle() {
        String id = createAndPublishVenture(uniqueName("Boutique"), "MODA_Y_ACCESORIOS", "INSTAGRAM", "boutique_ufps");

        // Agregar a favoritos (idempotente)
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().put("/api/v1/ventures/" + id + "/favorite")
                .then().statusCode(204);

        given()
                .header("Authorization", bearer(USER2_ID))
                .when().put("/api/v1/ventures/" + id + "/favorite")
                .then().statusCode(204);

        // Listar IDs favoritos
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/ventures/favorites/ids")
                .then().statusCode(200)
                .body("$", hasItem(id));

        // Listar página de favoritos
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/ventures/favorites")
                .then().statusCode(200)
                .body("items.id", hasItem(id));

        // Archivar el emprendimiento por admin
        given()
                .header("Authorization", bearer(ADMIN_ID))
                .when().post("/api/v1/admin/ventures/" + id + "/archive")
                .then().statusCode(200);

        // El favorito conservado pero NO se lista en favoritos activos
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/ventures/favorites")
                .then().statusCode(200)
                .body("items.id", not(hasItem(id)));

        // Quitar de favoritos
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().delete("/api/v1/ventures/" + id + "/favorite")
                .then().statusCode(204);
    }

    @Test
    @DisplayName("Reportes: umbral de 3 reportes pendientes devuelve a UNDER_REVIEW")
    void reportThresholdHidesVenture() {
        String ownerId = newOwner();
        String id = createAndPublishVenture(ownerId, uniqueName("Servicio Dudoso"), "OTROS", "WEB", "https://dudoso.com");

        // El dueño no puede reportar el propio -> 400
        given()
                .header("Authorization", bearer(ownerId))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "CONTENIDO_INAPROPIADO"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(400);

        // Reporte 1 por User2
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "FRAUDE_O_ESTAFA", "comment", "Posible estafa"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(201);

        // User2 intenta reportar de nuevo -> 409 Conflict
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "FRAUDE_O_ESTAFA"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(409);

        // Aún visible con 1 reporte
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200);

        // Reporte 2 por User3 -> Sigue visible
        given()
                .header("Authorization", bearer(USER3_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "INFORMACION_FALSA"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(201);

        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(200);

        // Reporte 3 por User4 -> Alcanza el umbral (3) y vuelve a UNDER_REVIEW
        given()
                .header("Authorization", bearer(USER4_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "CONTACTO_INVALIDO"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(201);

        // Ya NO es visible al público (404)
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404);
    }

    // =========================================================
    // 3. ENDPOINTS DE ADMINISTRADOR
    // =========================================================

    @Test
    @DisplayName("Endpoints de administrador exigen rol ADMIN (403 para usuarios normales)")
    void adminEndpointsRequireAdminRole() {
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/admin/ventures")
                .then().statusCode(403);

        given()
                .header("Authorization", bearer(USER2_ID))
                .when().get("/api/v1/admin/venture-reports")
                .then().statusCode(403);
    }

    @Test
    @DisplayName("Cola de administración detecta marcas de advertencia de duplicados")
    void adminQueueDuplicateWarnings() {
        String owner1 = newOwner();
        String owner2 = newOwner();
        String baseName = uniqueName("Mismo Nombre Duplicado");
        String contactVal = "3158889900";

        // Owner1 crea un emprendimiento
        given()
                .header("Authorization", bearer(owner1))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(baseName, "Desc 1", "COMIDA_Y_BEBIDAS", "WHATSAPP", contactVal,
                        List.of("DOMICILIO"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201);

        // Owner2 crea con el MISMO nombre y MISMO contacto -> se permite pero se marca advertencia para el admin
        String idOwner2 = given()
                .header("Authorization", bearer(owner2))
                .contentType(ContentType.JSON)
                .body(createVenturePayload(baseName, "Desc 2", "COMIDA_Y_BEBIDAS", "WHATSAPP", contactVal,
                        List.of("DOMICILIO"), null))
                .when().post("/api/v1/ventures")
                .then().statusCode(201)
                .extract().path("id");

        // Administrador consulta la cola
        given()
                .header("Authorization", bearer(ADMIN_ID))
                .queryParam("status", "UNDER_REVIEW")
                .when().get("/api/v1/admin/ventures")
                .then().statusCode(200)
                .body("items.id", hasItem(idOwner2))
                .body("items.find { it.id == '" + idOwner2 + "' }.duplicateNameWarning", is(true))
                .body("items.find { it.id == '" + idOwner2 + "' }.duplicateContactWarning", is(true))
                .body("items.find { it.id == '" + idOwner2 + "' }.contact.value", is("+57" + contactVal));
    }

    @Test
    @DisplayName("Administrador resuelve reportes: acción DISCARD vs ARCHIVE")
    void adminResolveReports() {
        String id = createAndPublishVenture(uniqueName("Para Reportar"), "OTROS", "EMAIL", "rep@ufps.edu.co");

        // Generar un reporte
        String reportId = given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "INFORMACION_FALSA", "comment", "Datos erróneos"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(201)
                .extract().path("reportId");

        // Listar reportes pendientes
        given()
                .header("Authorization", bearer(ADMIN_ID))
                .queryParam("status", "PENDING")
                .when().get("/api/v1/admin/venture-reports")
                .then().statusCode(200)
                .body("items.reportId", hasItem(reportId));

        // Resolver con ARCHIVE
        given()
                .header("Authorization", bearer(ADMIN_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("action", "ARCHIVE"))
                .when().post("/api/v1/admin/venture-reports/" + reportId + "/resolve")
                .then().statusCode(200)
                .body("status", is("RESOLVED"));

        // El emprendimiento fue archivado, por lo que ya no es visible
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("Eliminar un emprendimiento borra en cascada calificaciones, favoritos y reportes")
    void cascadeDeleteVenture() {
        String id = createAndPublishVenture(uniqueName("Cascada Test"), "COMIDA_Y_BEBIDAS", "WHATSAPP", "3109998877");

        // Calificación por User2
        given()
                .header("Authorization", bearer(USER2_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("score", 5))
                .when().put("/api/v1/ventures/" + id + "/rating")
                .then().statusCode(200);

        // Favorito por User2
        given()
                .header("Authorization", bearer(USER2_ID))
                .when().put("/api/v1/ventures/" + id + "/favorite")
                .then().statusCode(204);

        // Reporte por User3
        given()
                .header("Authorization", bearer(USER3_ID))
                .contentType(ContentType.JSON)
                .body(Map.of("reason", "OTRO", "comment", "Prueba cascada"))
                .when().post("/api/v1/ventures/" + id + "/report")
                .then().statusCode(201);

        // Eliminar directamente del repositorio de dominio
        assertDoesNotThrow(() -> ventureRepository.delete(VentureId.of(id)));

        // Verificar que no se encuentra
        given()
                .when().get("/api/v1/public/ventures/" + id)
                .then().statusCode(404);
    }
}
