package org.infoufps.api;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Perfil de prueba que fija PostgreSQL como motor en tiempo de construcción.
 * La URL real la aporta {@link EmbeddedPostgresResource} al arrancar.
 */
public class SchemaValidationProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.datasource.username", "postgres",
                "quarkus.datasource.password", "postgres",
                "quarkus.hibernate-orm.database.generation", "validate",
                "quarkus.flyway.migrate-at-start", "true",
                "quarkus.flyway.baseline-on-migrate", "false",
                "quarkus.devservices.enabled", "false"
        );
    }
}
