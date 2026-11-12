package org.infoufps.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoufps.application.account.AccessTokenIssuer;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.account.IAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ApiIntegrationTest {

    @Inject
    IAccountRepository accountRepository;

    @Inject
    AccessTokenIssuer accessTokenIssuer;

    private String adminId;
    private String userId;

    private String bearer(String accountId) {
        Account account = accountRepository.findById(AccountId.of(accountId)).orElseThrow();
        return "Bearer " + accessTokenIssuer.issue(account).token();
    }

    @BeforeEach
    void setUp() {
        if (!accountRepository.existsByEmail("admin@ufps.edu.co")) {
            Account admin = Account.register("admin@ufps.edu.co", "hashAdmin", "Administrador", Role.ADMIN);
            accountRepository.save(admin);
            adminId = admin.getAccountId().value();
        } else {
            adminId = accountRepository.findByEmail("admin@ufps.edu.co").orElseThrow().getAccountId().value();
        }

        if (!accountRepository.existsByEmail("alumno@ufps.edu.co")) {
            Account user = Account.register("alumno@ufps.edu.co", "hashUser", "Alumno Test", Role.USER);
            accountRepository.save(user);
            userId = user.getAccountId().value();
        } else {
            userId = accountRepository.findByEmail("alumno@ufps.edu.co").orElseThrow().getAccountId().value();
        }
    }

    @Test
    @DisplayName("Catálogos públicos devuelven 200 OK")
    void testPublicCatalogs() {
        given()
                .when().get("/api/v1/public/catalogos/facultades")
                .then().statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].clave", notNullValue());

        given()
                .when().get("/api/v1/public/catalogos/tipos-materia")
                .then().statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .when().get("/api/v1/public/catalogos/creditos")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Rutas protegidas devuelven 401 si no hay usuario autenticado")
    void testUnauthorizedWithoutHeader() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/user/profesores")
                .then().statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Rutas administrativas devuelven 403 si el rol no es ADMIN")
    void testForbiddenForNormalUser() {
        given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(403);
    }

    @Test
    @DisplayName("Un valor de enum inválido responde 400 en lugar de 500")
    void testInvalidEnumReturnsBadRequest() {        given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Docente Inválido",
                            "facultad": "FACULTAD_INEXISTENTE"
                        }
                        """)
                .when().post("/api/v1/user/profesores")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("La validación de entrada responde 400 con el formato único")
    void testValidationReturnsSingleErrorFormat() {
        given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"   \"}")
                .when().post("/api/v1/user/profesores")
                .then().statusCode(400)
                .body("error", equalTo("BAD_REQUEST"))
                .body("status", equalTo(400))
                .body("message", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("La búsqueda de cuentas por email no distingue mayúsculas")
    void testEmailLookupIsCaseInsensitive() {
        org.junit.jupiter.api.Assertions.assertTrue(
                accountRepository.existsByEmail("ADMIN@UFPS.EDU.CO"));
        org.junit.jupiter.api.Assertions.assertTrue(
                accountRepository.findByEmail("ADMIN@UFPS.EDU.CO").isPresent());
    }

    @Test
    @DisplayName("El índice único de email se traduce a un conflicto de dominio")
    void testDuplicateEmailIsTranslatedToConflict() {
        Account first = Account.register("duplicado@ufps.edu.co", "hash", "Primero");
        accountRepository.save(first);

        Account second = Account.register("DUPLICADO@UFPS.EDU.CO", "hash", "Segundo");

        org.junit.jupiter.api.Assertions.assertThrows(
                org.infoufps.domain.shared.exceptions.EmailAlreadyExistsException.class,
                () -> accountRepository.save(second));
    }

    @Test
    @DisplayName("Las reseñas públicas de un profesor no publicado responden 404")
    void testPublicReviewsOfUnpublishedProfesorReturnNotFound() {
        String profesorId = given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Profesor En Revisión Público",
                            "facultad": "INGENIERIA"
                        }
                        """)
                .when().post("/api/v1/user/profesores")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().get("/api/v1/public/profesores/%s/resenas".formatted(profesorId))
                .then().statusCode(404)
                .body("error", equalTo("NOT_FOUND"));
    }

    @Test
    @DisplayName("Flujo completo: Admin crea materia y profesor, usuario propone y reseña, búsqueda pública")
    void testEndToEndWorkflow() {
        // 1. Admin crea materia publicada
        String materiaId = given()
                .header("Authorization", bearer(adminId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Arquitectura de Software",
                            "codigo": "1150801",
                            "creditos": 4,
                            "tipo": "CARRERA"
                        }
                        """)
                .when().post("/api/v1/admin/materias")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("codigo", equalTo("1150801"))
                .extract().path("id");

        // 2. Admin crea profesor publicado
        String profesorId = given()
                .header("Authorization", bearer(adminId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Fernando José Martínez",
                            "facultad": "INGENIERIA",
                            "email": "fernando.martinez@ufps.edu.co",
                            "materiasIds": ["%s"]
                        }
                        """.formatted(materiaId))
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("nombre", equalTo("Fernando José Martínez"))
                .body("email", equalTo("fernando.martinez@ufps.edu.co"))
                .extract().path("id");

        // 3. Intento de crear duplicado por admin devuelve 409 Conflict
        given()
                .header("Authorization", bearer(adminId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Martínez Fernando José",
                            "facultad": "INGENIERIA",
                            "email": "otro@ufps.edu.co"
                        }
                        """)
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(409)
                .body("error", equalTo("CONFLICT"));

        // 4. Usuario autenticado crea reseña para el profesor publicado
        String resenaId = given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "tipoDestino": "PROFESOR",
                            "profesorId": "%s",
                            "calificacion": 5,
                            "comentario": "Excelente profesor, muy claro en sus clases",
                            "materiaCursada": "Arquitectura de Software"
                        }
                        """.formatted(profesorId))
                .when().post("/api/v1/user/resenas")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("calificacion", equalTo(5))
                .body("estado", equalTo("EN_REVISION"))
                .extract().path("id");

        // 5. Usuario intenta crear segunda reseña activa para el mismo profesor -> 409 Conflict
        given()
                .header("Authorization", bearer(userId))
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "tipoDestino": "PROFESOR",
                            "profesorId": "%s",
                            "calificacion": 4,
                            "comentario": "Otra reseña duplicada"
                        }
                        """.formatted(profesorId))
                .when().post("/api/v1/user/resenas")
                .then().statusCode(409)
                .body("error", equalTo("CONFLICT"));

        // 6. Consultar reseñas propias (el autor ve su reseña en revisión)
        given()
                .header("Authorization", bearer(userId))
                .when().get("/api/v1/user/resenas")
                .then().statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", equalTo(resenaId));

        // 7. Admin publica la reseña a través de la cola de moderación
        given()
                .header("Authorization", bearer(adminId))
                .when().put("/api/v1/admin/moderacion/resenas/%s/publicar".formatted(resenaId))
                .then().statusCode(200)
                .body("estado", equalTo("PUBLICADO"));

        // 8. Búsqueda pública de profesor: ahora promedioCalificacion = 5.0 y totalResenas = 1
        given()
                .queryParam("q", "martinez fernando")
                .when().get("/api/v1/public/profesores")
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("items[0].nombre", equalTo("Fernando José Martínez"))
                .body("items[0].promedioCalificacion", equalTo(5.0f))
                .body("items[0].totalResenas", equalTo(1))
                .body("items[0]", not(hasKey("email")))
                .body("items[0]", not(hasKey("estado")))
                .body("items[0]", not(hasKey("proponenteId")));

        // 9. Detalle público del profesor
        given()
                .when().get("/api/v1/public/profesores/%s".formatted(profesorId))
                .then().statusCode(200)
                .body("id", equalTo(profesorId))
                .body("promedioCalificacion", equalTo(5.0f))
                .body("materias.size()", greaterThan(0))
                .body("$", not(hasKey("email")))
                .body("$", not(hasKey("estado")))
                .body("$", not(hasKey("proponenteId")));

        // 10. Reseñas públicas del profesor
        given()
                .when().get("/api/v1/public/profesores/%s/resenas".formatted(profesorId))
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("items[0].id", equalTo(resenaId))
                .body("items[0].autorNombre", notNullValue())
                .body("items[0]", not(hasKey("autorId")))
                .body("items[0]", not(hasKey("estado")));

        // 11. Búsqueda global
        given()
                .queryParam("q", "arquitectura")
                .when().get("/api/v1/public/busqueda-global")
                .then().statusCode(200)
                .body("materias.size()", greaterThan(0))
                .body("profesores[0]", not(hasKey("email")))
                .body("profesores[0]", not(hasKey("estado")))
                .body("materias[0]", not(hasKey("estado")))
                .body("materias[0]", not(hasKey("proponenteId")));

        // 12. Listado y detalle público de materias sin datos internos
        given()
                .queryParam("q", "arquitectura")
                .when().get("/api/v1/public/materias")
                .then().statusCode(200)
                .body("items[0]", not(hasKey("estado")))
                .body("items[0]", not(hasKey("proponenteId")));

        given()
                .when().get("/api/v1/public/materias/%s".formatted(materiaId))
                .then().statusCode(200)
                .body("id", equalTo(materiaId))
                .body("$", not(hasKey("estado")))
                .body("$", not(hasKey("proponenteId")));

        // 13. Consultar recurso inexistente -> 404
        given()
                .when().get("/api/v1/public/profesores/inexistente-uuid")
                .then().statusCode(404)
                .body("error", equalTo("NOT_FOUND"));
    }
}

