package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Locale;

/**
 * Tipos de evento disponibles en el calendario.
 * Cada valor expone el nombre visible usado por el frontend.
 */
public enum EventType {

    CULTURAL("Cultural"),
    ACADEMICO("Académico"),
    INSTITUCIONAL("Institucional"),
    CONVOCATORIA("Convocatoria"),
    BIENESTAR("Bienestar"),
    DEPORTIVO("Deportivo"),
    GENERAL("General"),
    CALENDARIO_ACADEMICO("Calendario académico");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    /**
     * @return nombre visible para el usuario.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return valor persistido y usado por la API.
     */
    public String getValue() {
        return name();
    }

    /**
     * Indica si el tipo solo puede ser creado por un administrador.
     *
     * @return true para INSTITUCIONAL y CALENDARIO_ACADEMICO.
     */
    public boolean isAdminOnly() {
        return this == INSTITUCIONAL || this == CALENDARIO_ACADEMICO;
    }

    /**
     * Convierte un texto de entrada en un tipo de evento válido.
     * Acepta el valor del enum sin distinguir mayúsculas/minúsculas.
     *
     * @param value texto recibido desde la API.
     * @return tipo de evento.
     * @throws DomainException si el valor es nulo, vacío o desconocido.
     */
    public static EventType from(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El tipo de evento es obligatorio");
        }
        try {
            return EventType.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de evento inválido: " + value);
        }
    }

    /**
     * Convierte un texto opcional en tipo de evento. Si no llega, usa GENERAL.
     *
     * @param value texto recibido desde la API.
     * @return tipo de evento válido o GENERAL.
     */
    public static EventType fromOrDefault(String value) {
        if (value == null || value.isBlank()) {
            return GENERAL;
        }
        return from(value);
    }
}
