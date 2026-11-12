package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Objeto de Valor (Value Object) inmutable que identifica un evento del calendario.
 * Encapsula un UUID para garantizar identificadores fuertemente tipados y no vacíos.
 */
public final class EventId {

    /** Valor textual del identificador. */
    private final String value;

    private EventId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El EventId no puede estar vacío.");
        }
        this.value = value.strip();
    }

    /**
     * Genera un nuevo identificador único (UUID v4).
     *
     * @return nuevo EventId.
     */
    public static EventId generate() {
        return new EventId(UUID.randomUUID().toString());
    }

    /**
     * Reconstruye un EventId a partir de un valor existente.
     *
     * @param value valor textual del identificador.
     * @return EventId asociado al valor.
     */
    public static EventId of(String value) {
        return new EventId(value);
    }

    /**
     * @return representación textual del identificador.
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventId eventId)) return false;
        return Objects.equals(value, eventId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
