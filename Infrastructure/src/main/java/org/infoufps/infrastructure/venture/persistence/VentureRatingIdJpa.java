package org.infoufps.infrastructure.venture.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta para una calificación: cuenta + emprendimiento.
 */
@Embeddable
public class VentureRatingIdJpa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "venture_id", length = 36, nullable = false)
    private String ventureId;

    public VentureRatingIdJpa() {}

    public VentureRatingIdJpa(String accountId, String ventureId) {
        this.accountId = accountId;
        this.ventureId = ventureId;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getVentureId() { return ventureId; }
    public void setVentureId(String ventureId) { this.ventureId = ventureId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VentureRatingIdJpa that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(ventureId, that.ventureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, ventureId);
    }
}

