package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fuertemente tipado e inmutable para un emprendimiento.
 */
public final class VentureId {

    private final String value;

    private VentureId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El VentureId no puede estar vacío");
        }
        this.value = value.strip();
    }

    /**
     * Genera un nuevo identificador UUID v4.
     */
    public static VentureId generate() {
        return new VentureId(UUID.randomUUID().toString());
    }

    /**
     * Reconstruye un VentureId a partir de su representación en cadena.
     */
    public static VentureId of(String value) {
        return new VentureId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VentureId that)) return false;
        return Objects.equals(value, that.value);
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

