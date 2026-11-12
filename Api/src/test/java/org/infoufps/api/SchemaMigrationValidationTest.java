package org.infoufps.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba que las migraciones Flyway producen un esquema compatible con el
 * mapeo de Hibernate y con las reglas exigidas al módulo académico.
 * <p>
 * Se ejecuta sobre PostgreSQL embebido porque H2 no soporta índices parciales.
 */
@QuarkusTest
@QuarkusTestResource(value = EmbeddedPostgresResource.class, restrictToAnnotatedClass = true)
@TestProfile(SchemaValidationProfile.class)
@EnabledOnOs(OS.WINDOWS)
@DisplayName("Migraciones Flyway compatibles con Hibernate")
class SchemaMigrationValidationTest {

    @Inject
    DataSource dataSource;

    @Inject
    jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("Flyway aplica migraciones y Hibernate valida el esquema")
    void migrationsAreAppliedAndValidated() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE")) {
            assertTrue(result.next());
            assertTrue(result.getInt(1) >= 10, "Deben aplicarse al menos diez migraciones");
        }
    }

    @Test
    @DisplayName("La tabla comments tiene target_type, target_id y status, y content_id fue eliminada")
    void genericCommentsColumnsExist() throws SQLException {
        assertEquals("character varying", scalar(
                "SELECT data_type FROM information_schema.columns " +
                        "WHERE table_name = 'comments' AND column_name = 'target_type'"));
        assertEquals("character varying", scalar(
                "SELECT data_type FROM information_schema.columns " +
                        "WHERE table_name = 'comments' AND column_name = 'target_id'"));
        assertEquals("character varying", scalar(
                "SELECT data_type FROM information_schema.columns " +
                        "WHERE table_name = 'comments' AND column_name = 'status'"));
        assertEquals(0, count(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_name = 'comments' AND column_name = 'content_id'"));
    }

    @Test
    @DisplayName("La columna status de accounts es obligatoria y vale ACTIVE por defecto")
    void accountStatusColumnExists() throws SQLException {
        assertEquals("character varying", scalar(
                "SELECT data_type FROM information_schema.columns " +
                        "WHERE table_name = 'accounts' AND column_name = 'status'"));
        assertEquals("NO", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'accounts' AND column_name = 'status'"));
        assertEquals("'ACTIVE'::character varying", scalar(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE table_name = 'accounts' AND column_name = 'status'"));
    }

    @Test
    @DisplayName("Columna version existe en todas las entidades principales")
    void versionColumnExistsOnAllMainTables() throws SQLException {
        String[] tables = {"profesores", "materias", "resenas", "events", "accounts", "contents", "comments", "ventures", "forum_threads", "forum_reports"};
        for (String table : tables) {
            assertEquals("bigint", scalar(
                    "SELECT data_type FROM information_schema.columns " +
                            "WHERE table_name = '" + table + "' AND column_name = 'version'"));
        }
    }

    @Test
    @DisplayName("Índice único parcial: prohíbe duplicados PUBLICADO pero permite en EN_REVISION")
    void partialUniqueIndexEnforcedOnPostgres() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Limpieza previa
            stmt.executeUpdate("DELETE FROM profesores WHERE proponente_id = 'test-partial-index'");

            // Dos en revisión con misma facultad y nombre normalizado -> permitido
            stmt.executeUpdate("INSERT INTO profesores (profesor_id, nombre, nombre_normalizado, facultad, estado, proponente_id, created_at, version) " +
                    "VALUES ('p1', 'Juan Perez', 'juan perez', 'INGENIERIA', 'EN_REVISION', 'test-partial-index', NOW(), 0)");
            stmt.executeUpdate("INSERT INTO profesores (profesor_id, nombre, nombre_normalizado, facultad, estado, proponente_id, created_at, version) " +
                    "VALUES ('p2', 'Juan C Perez', 'juan perez', 'INGENIERIA', 'EN_REVISION', 'test-partial-index', NOW(), 0)");

            // Uno publicado -> permitido
            stmt.executeUpdate("INSERT INTO profesores (profesor_id, nombre, nombre_normalizado, facultad, estado, proponente_id, created_at, version) " +
                    "VALUES ('p3', 'Juan Carlos Perez', 'juan carlos perez', 'INGENIERIA', 'PUBLICADO', 'test-partial-index', NOW(), 0)");

            // Segundo publicado con mismo nombre_normalizado y facultad -> debe fallar por índice parcial
            org.junit.jupiter.api.Assertions.assertThrows(SQLException.class, () -> {
                stmt.executeUpdate("INSERT INTO profesores (profesor_id, nombre, nombre_normalizado, facultad, estado, proponente_id, created_at, version) " +
                        "VALUES ('p4', 'Juan Carlos Perez Jr', 'juan carlos perez', 'INGENIERIA', 'PUBLICADO', 'test-partial-index', NOW(), 0)");
            });
        }
    }

    @Test
    @DisplayName("Consulta JPQL con CASE se ejecuta con éxito en PostgreSQL real")
    void jpqlWithCaseExecutedAgainstPostgres() {
        var query = entityManager.createQuery(
                "SELECT e FROM EventJpa e WHERE e.status = :status " +
                        "ORDER BY e.startDate ASC, CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, e.startTime ASC, LOWER(e.title) ASC",
                org.infoufps.infrastructure.calendar.persistence.EventJpa.class);
        query.setParameter("status", org.infoufps.domain.calendar.Status.PUBLISHED);
        query.setMaxResults(10);
        var results = query.getResultList();
        org.junit.jupiter.api.Assertions.assertNotNull(results);
    }

    @Test
    @DisplayName("El índice único parcial de profesores publicados existe")
    void publishedProfessorUniqueIndexExists() throws SQLException {
        assertEquals(1, count(
                "SELECT COUNT(*) FROM pg_indexes " +
                        "WHERE indexname = 'idx_unique_published_profesor_nombre_facultad'"));
    }

    @Test
    @DisplayName("El índice único de email no distingue mayúsculas")
    void caseInsensitiveEmailIndexExists() throws SQLException {
        assertEquals(1, count(
                "SELECT COUNT(*) FROM pg_indexes " +
                        "WHERE indexname = 'idx_accounts_email_lower'"));
    }

    @Test
    @DisplayName("La base de datos rechaza dos emails que solo difieren en mayúsculas")
    void caseInsensitiveEmailIndexRejectsDuplicates() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM accounts WHERE account_id IN ('case-email-1', 'case-email-2')");
            stmt.executeUpdate("INSERT INTO accounts " +
                    "(account_id, name, email, password_hash, role, created_at, version) " +
                    "VALUES ('case-email-1', 'Uno', 'Caso@UFPS.edu.co', 'hash', 'USER', NOW(), 0)");

            org.junit.jupiter.api.Assertions.assertThrows(SQLException.class, () ->
                    stmt.executeUpdate("INSERT INTO accounts " +
                            "(account_id, name, email, password_hash, role, created_at, version) " +
                            "VALUES ('case-email-2', 'Dos', 'caso@ufps.edu.co', 'hash', 'USER', NOW(), 0)"));
        }
    }

    @Test
    @DisplayName("Las reseñas no permiten borrar profesores o materias con reseñas")
    void reviewForeignKeysUseRestrict() throws SQLException {
        assertEquals("r", scalar(
                "SELECT confdeltype FROM pg_constraint WHERE conname = 'fk_resena_profesor'"));
        assertEquals("r", scalar(
                "SELECT confdeltype FROM pg_constraint WHERE conname = 'fk_resena_materia'"));
    }

    @Test
    @DisplayName("Los contadores de interacción son obligatorios y valen cero por defecto")
    void interactionCountersAreNotNullWithZeroDefault() throws SQLException {
        assertEquals("NO", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'likes_count'"));
        assertEquals("NO", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'comments_count'"));
        assertEquals("0", scalar(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'likes_count'"));
        assertEquals("0", scalar(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'comments_count'"));
    }

    @Test
    @DisplayName("La tabla refresh_tokens guarda hashes y tiene índices de búsqueda")
    void refreshTokensSchemaExists() throws SQLException {
        assertEquals("character varying", scalar(
                "SELECT data_type FROM information_schema.columns " +
                        "WHERE table_name = 'refresh_tokens' AND column_name = 'token_hash'"));
        assertEquals("64", scalar(
                "SELECT character_maximum_length FROM information_schema.columns " +
                        "WHERE table_name = 'refresh_tokens' AND column_name = 'token_hash'"));
        assertEquals("YES", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'refresh_tokens' AND column_name = 'revoked_at'"));
        assertEquals(1, count(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_refresh_tokens_hash'"));
        assertEquals(1, count(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_refresh_tokens_account'"));
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private String scalar(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next(), "La consulta no devolvió filas: " + sql);
            return result.getString(1);
        }
    }
}
