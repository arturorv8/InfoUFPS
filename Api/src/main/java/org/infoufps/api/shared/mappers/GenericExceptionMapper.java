package org.infoufps.api.shared.mappers;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Traduce cualquier error no controlado a HTTP 500 sin exponer detalles internos.
 * <p>
 * Las respuestas de {@link WebApplicationException} se conservan tal cual para no
 * alterar los códigos HTTP que ya genera la capa REST.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            return webApplicationException.getResponse();
        }

        LOG.error("Error no controlado al procesar la petición", exception);

        ErrorResponse body = ErrorResponse.of("INTERNAL_SERVER_ERROR", "Ocurrió un error interno", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
