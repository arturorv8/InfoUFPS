package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

public final class ProfesorId {

    private final String value;

    private ProfesorId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El ID del profesor no puede estar vacío");
        }
        this.value = value.strip();
    }

    public static ProfesorId generate() {
        return new ProfesorId(UUID.randomUUID().toString());
    }

    public static ProfesorId of(String value) {
        return new ProfesorId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfesorId that)) return false;
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

