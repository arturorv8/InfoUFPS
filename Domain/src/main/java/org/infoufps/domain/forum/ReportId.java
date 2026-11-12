package org.infoufps.domain.forum;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único e inmutable de un reporte de moderación en el foro.
 */
public final class ReportId implements Serializable {

    private final String value;

    private ReportId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El identificador del reporte no puede ser nulo ni vacío");
        }
        this.value = value.strip();
    }

    public static ReportId of(String value) {
        return new ReportId(value);
    }

    public static ReportId generate() {
        return new ReportId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportId reportId)) return false;
        return Objects.equals(value, reportId.value);
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

