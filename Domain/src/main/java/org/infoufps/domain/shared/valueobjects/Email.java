package org.infoufps.domain.shared.valueobjects;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,24}$"
    );

    private final String value;

    private Email(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El email no puede estar vacío");
        }
        String normalized = value.strip().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new DomainException("El formato del email es inválido: " + value);
        }
        this.value = normalized;
    }

    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new Email(value);
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return of(value).value();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
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

