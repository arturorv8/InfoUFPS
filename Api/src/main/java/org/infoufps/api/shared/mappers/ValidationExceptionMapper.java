package org.infoufps.api.shared.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Traduce los errores de validación de Jakarta Bean Validation al formato único
 * de error de la API.
 * <p>
 * Este mapper es más específico que el de {@code ValidationException} incluido
 * por Quarkus, por lo que las respuestas de {@code @Valid} conservan la misma
 * estructura {@link ErrorResponse} que el resto de la API.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations() == null
                ? ""
                : exception.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));

        String message = detail.isBlank() ? "Datos de entrada inválidos" : detail;
        ErrorResponse body = ErrorResponse.of("BAD_REQUEST", message, 400);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
