package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Puerto de Dominio para la persistencia y agregación de calificaciones de emprendimientos.
 */
public interface IVentureRatingRepository {

    /**
     * Guarda o actualiza una calificación.
     */
    VentureRating save(VentureRating rating);

    /**
     * Busca la calificación otorgada por una cuenta a un emprendimiento específico.
     */
    Optional<VentureRating> findByAccountIdAndVentureId(AccountId accountId, VentureId ventureId);

    /**
     * Elimina la calificación otorgada por una cuenta a un emprendimiento.
     */
    void delete(AccountId accountId, VentureId ventureId);

    /**
     * Obtiene el resumen de calificación (promedio y total) para un emprendimiento dado.
     */
    RatingSummary getRatingSummary(VentureId ventureId);

    /**
     * Obtiene en bloque el resumen de calificaciones para una lista de emprendimientos.
     */
    Map<VentureId, RatingSummary> getRatingSummaries(List<VentureId> ventureIds);
}

