package org.infoufps.api.shared.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.EmailAlreadyExistsException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.shared.exceptions.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionMappersTest {

    @Test
    @DisplayName("Un error de dominio responde 400 con el formato único")
    void mapsDomainExceptionToBadRequest() {
        Response response = new DomainExceptionMapper().toResponse(new DomainException("dato inválido"));

        assertError(response, 400, "BAD_REQUEST", "dato inválido");
    }

    @Test
    @DisplayName("Un recurso inexistente responde 404")
    void mapsNotFoundExceptionToNotFound() {
        Response response = new NotFoundExceptionMapper().toResponse(new NotFoundException("no existe"));

        assertError(response, 404, "NOT_FOUND", "no existe");
    }

    @Test
    @DisplayName("ResourceNotFoundException hereda de NotFoundException")
    void resourceNotFoundIsNotFound() {
        Response response = new NotFoundExceptionMapper()
                .toResponse(new ResourceNotFoundException("no existe"));

        assertError(response, 404, "NOT_FOUND", "no existe");
    }

    @Test
    @DisplayName("Un conflicto de negocio responde 409")
    void mapsConflictExceptionToConflict() {
        Response response = new ConflictExceptionMapper().toResponse(new ConflictException("ya existe"));

        assertError(response, 409, "CONFLICT", "ya existe");
    }

    @Test
    @DisplayName("DuplicateResourceException hereda de ConflictException")
    void duplicateResourceIsConflict() {
        Response response = new ConflictExceptionMapper()
                .toResponse(new DuplicateResourceException("duplicado"));

        assertError(response, 409, "CONFLICT", "duplicado");
    }

    @Test
    @DisplayName("Un email duplicado responde 409")
    void mapsEmailAlreadyExistsToConflict() {
        Response response = new ConflictExceptionMapper()
                .toResponse(new EmailAlreadyExistsException());

        assertError(response, 409, "CONFLICT", "Ya existe una cuenta con ese email");
    }

    @Test
    @DisplayName("La falta de permisos responde 403")
    void mapsForbiddenExceptionToForbidden() {
        Response response = new ForbiddenExceptionMapper().toResponse(new ForbiddenException("prohibido"));

        assertError(response, 403, "FORBIDDEN", "prohibido");
    }

    @Test
    @DisplayName("La falta de autenticación responde 401")
    void mapsUnauthorizedExceptionToUnauthorized() {
        Response response = new UnauthorizedExceptionMapper().toResponse(new UnauthorizedException("sin sesión"));

        assertError(response, 401, "UNAUTHORIZED", "sin sesión");
    }

    @Test
    @DisplayName("El límite diario responde 429")
    void mapsDailyLimitToTooManyRequests() {
        Response response = new DailyLimitExceededExceptionMapper()
                .toResponse(new DailyLimitExceededException("límite alcanzado"));

        assertError(response, 429, "DAILY_LIMIT_EXCEEDED", "límite alcanzado");
    }

    @Test
    @DisplayName("Un error inesperado responde 500 sin exponer detalles internos")
    void hidesInternalDetails() {
        Response response = new GenericExceptionMapper().toResponse(new RuntimeException("detalle interno sensible"));

        assertEquals(500, response.getStatus());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getEntity());
        assertEquals("INTERNAL_SERVER_ERROR", body.error());
        assertEquals(500, body.status());
        assertFalse(body.message().contains("detalle interno sensible"));
    }

    @Test
    @DisplayName("Un error de validación responde 400 con el formato único")
    void mapsValidationExceptionToBadRequest() {
        Response response = new ValidationExceptionMapper()
                .toResponse(new ConstraintViolationException("entrada inválida", java.util.Set.of()));

        assertError(response, 400, "BAD_REQUEST", "Datos de entrada inválidos");
    }

    @Test
    @DisplayName("Las respuestas de WebApplicationException se conservan")
    void preservesWebApplicationExceptionResponse() {
        Response original = Response.status(Response.Status.NOT_FOUND).build();

        Response response = new GenericExceptionMapper().toResponse(new WebApplicationException(original));

        assertEquals(404, response.getStatus());
    }

    private void assertError(Response response, int status, String error, String message) {
        assertEquals(status, response.getStatus());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getEntity());
        assertEquals(error, body.error());
        assertEquals(message, body.message());
        assertEquals(status, body.status());
        assertNotNull(body.timestamp());
    }
}
