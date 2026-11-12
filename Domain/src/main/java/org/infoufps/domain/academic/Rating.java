package org.infoufps.domain.academic;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;

public final class Rating {

    private final int value;

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    private Rating(int value) {
        if (value < MIN_RATING || value > MAX_RATING) {
            throw new DomainException("La calificación debe estar entre " + MIN_RATING + " y " + MAX_RATING);
        }
        this.value = value;
    }

    public static Rating of(int value) {
        return new Rating(value);
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating rating)) return false;
        return value == rating.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

