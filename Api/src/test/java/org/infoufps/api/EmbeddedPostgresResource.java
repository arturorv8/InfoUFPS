package org.infoufps.api;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Levanta un PostgreSQL embebido para validar las migraciones Flyway contra el
 * mapeo real de Hibernate.
 */
public class EmbeddedPostgresResource implements QuarkusTestResourceLifecycleManager {

    private EmbeddedPostgres postgres;

    @Override
    public Map<String, String> start() {
        try {
            postgres = EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo iniciar PostgreSQL embebido", e);
        }

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl("postgres", "postgres"));
        return config;
    }

    @Override
    public void stop() {
        if (postgres != null) {
            try {
                postgres.close();
            } catch (IOException ignored) {
                // El proceso de prueba ya terminó; no hay nada más que liberar.
            }
        }
    }
}
