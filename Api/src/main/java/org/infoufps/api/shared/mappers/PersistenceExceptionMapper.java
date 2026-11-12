package org.infoufps.api.shared.mappers;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;

/**
 * Traduce violaciones de unicidad de la base de datos a HTTP 409.
 * <p>
 * Cualquier otro error de persistencia se delega al manejador genérico para
 * responder 500 sin filtrar detalles internos.
 */
@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final GenericExceptionMapper fallback = new GenericExceptionMapper();

    @Override
    public Response toResponse(PersistenceException exception) {
        if (isUniqueConstraintViolation(exception)) {
            ErrorResponse body = ErrorResponse.of("CONFLICT", "El recurso ya existe", 409);
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        }

        return fallback.toResponse(exception);
    }

    private boolean isUniqueConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }
            if (current instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_VIOLATION_SQL_STATE.equals(constraintViolation.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
