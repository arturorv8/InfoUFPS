package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único e inmutable de un hilo del foro.
 */
public final class ThreadId implements Serializable {

    private final String value;

    private ThreadId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El identificador del hilo no puede ser nulo ni vacío");
        }
        this.value = value.strip();
    }

    public static ThreadId of(String value) {
        return new ThreadId(value);
    }

    public static ThreadId generate() {
        return new ThreadId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreadId threadId)) return false;
        return Objects.equals(value, threadId.value);
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

