package org.infoufps.api.shared.mappers;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PersistenceExceptionMapperTest {

    private final PersistenceExceptionMapper mapper = new PersistenceExceptionMapper();

    @Test
    @DisplayName("Una violación de unicidad de PostgreSQL responde 409")
    void mapsUniqueViolationToConflict() {
        PersistenceException exception = new PersistenceException(
                "could not execute statement",
                new SQLException("duplicate key value violates unique constraint", "23505"));

        Response response = mapper.toResponse(exception);

        assertEquals(409, response.getStatus());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getEntity());
        assertEquals("CONFLICT", body.error());
        assertEquals(409, body.status());
    }

    @Test
    @DisplayName("Una violación de unicidad de Hibernate responde 409")
    void mapsHibernateConstraintViolationToConflict() {
        ConstraintViolationException exception = new ConstraintViolationException(
                "could not execute statement",
                new SQLException("duplicate key value violates unique constraint", "23505"),
                "uk_profesor");

        Response response = mapper.toResponse(exception);

        assertEquals(409, response.getStatus());
    }

    @Test
    @DisplayName("Otro error de persistencia responde 500 sin exponer detalles")
    void mapsOtherPersistenceErrorToInternalServerError() {
        PersistenceException exception = new PersistenceException(
                "fallo interno de base de datos",
                new SQLException("syntax error", "42601"));

        Response response = mapper.toResponse(exception);

        assertEquals(500, response.getStatus());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getEntity());
        assertEquals("INTERNAL_SERVER_ERROR", body.error());
        assertFalse(body.message().contains("fallo interno de base de datos"));
    }
}
