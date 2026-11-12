package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

public final class MateriaId {

    private final String value;

    private MateriaId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El ID de la materia no puede estar vacío");
        }
        this.value = value.strip();
    }

    public static MateriaId generate() {
        return new MateriaId(UUID.randomUUID().toString());
    }

    public static MateriaId of(String value) {
        return new MateriaId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MateriaId that)) return false;
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

