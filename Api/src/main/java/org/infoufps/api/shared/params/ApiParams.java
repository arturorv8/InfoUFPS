package org.infoufps.api.shared.params;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Utilidades de validación de parámetros HTTP.
 */
public final class ApiParams {

    private ApiParams() {
    }

    /**
     * Parsea una fecha ISO obligatoria.
     *
     * @param value valor recibido.
     * @param name  nombre del parámetro para el mensaje.
     * @return fecha válida.
     */
    public static LocalDate requiredDate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El parámetro " + name + " es obligatorio");
        }
        try {
            return LocalDate.parse(value.strip());
        } catch (DateTimeParseException ex) {
            throw new DomainException("El parámetro " + name + " no es una fecha válida");
        }
    }

    /**
     * Parsea un entero opcional.
     *
     * @param value        valor recibido.
     * @param name         nombre del parámetro.
     * @param defaultValue valor por defecto.
     * @return entero válido.
     */
    public static int optionalInt(String value, String name, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException ex) {
            throw new DomainException("El parámetro " + name + " no es un número válido");
        }
    }
}
