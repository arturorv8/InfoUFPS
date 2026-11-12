package org.infoufps.api.shared.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.infoufps.domain.shared.exceptions.ConflictException;

/**
 * Traduce conflictos de negocio a HTTP 409.
 */
@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Override
    public Response toResponse(ConflictException exception) {
        ErrorResponse body = ErrorResponse.of("CONFLICT", exception.getMessage(), 409);
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
