package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de VentureRating")
class VentureRatingTest {

    @Test
    @DisplayName("Creación y actualización válida con puntaje de 1 a 5")
    void validRatingAndScoreUpdate() {
        AccountId accountId = AccountId.generate();
        VentureId ventureId = VentureId.generate();

        VentureRating rating = VentureRating.create(accountId, ventureId, 4);
        assertEquals(accountId, rating.getAccountId());
        assertEquals(ventureId, rating.getVentureId());
        assertEquals(4, rating.getScore());
        assertNotNull(rating.getCreatedAt());
        assertNotNull(rating.getUpdatedAt());

        rating.updateScore(5);
        assertEquals(5, rating.getScore());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 10, 100})
    @DisplayName("Rechaza puntajes fuera del rango [1, 5]")
    void invalidScoreThrowsDomainException(int invalidScore) {
        AccountId accountId = AccountId.generate();
        VentureId ventureId = VentureId.generate();

        assertThrows(DomainException.class, () -> VentureRating.create(accountId, ventureId, invalidScore));

        VentureRating validRating = VentureRating.create(accountId, ventureId, 3);
        assertThrows(DomainException.class, () -> validRating.updateScore(invalidScore));
    }
}

