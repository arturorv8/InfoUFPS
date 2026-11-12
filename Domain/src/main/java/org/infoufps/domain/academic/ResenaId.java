package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

public final class ResenaId {

    private final String value;

    private ResenaId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El ID de la reseña no puede estar vacío");
        }
        this.value = value.strip();
    }

    public static ResenaId generate() {
        return new ResenaId(UUID.randomUUID().toString());
    }

    public static ResenaId of(String value) {
        return new ResenaId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResenaId that)) return false;
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

