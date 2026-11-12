package org.infoufps.domain.account;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.UUID;

public final class AccountId {
    private final String value;

    private AccountId(String value) {
        if (value == null || value.isBlank())
            throw new DomainException("El accountId no puede estar vacío");
        this.value = value.strip();
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID().toString());
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountId accountId = (AccountId) o;
        return value.equals(accountId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
