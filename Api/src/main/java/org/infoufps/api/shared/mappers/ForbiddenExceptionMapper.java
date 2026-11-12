package org.infoufps.api.shared.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.infoufps.domain.shared.exceptions.ForbiddenException;

/**
 * Traduce la falta de permisos a HTTP 403.
 */
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException exception) {
        ErrorResponse body = ErrorResponse.of("FORBIDDEN", exception.getMessage(), 403);
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
